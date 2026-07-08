package no.nav.hjelpemidler.delbestilling.delbestilling

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import no.nav.hjelpemidler.delbestilling.common.Delbestilling
import no.nav.hjelpemidler.delbestilling.common.DelbestillingSak
import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.common.Lager
import no.nav.hjelpemidler.delbestilling.common.Levering
import no.nav.hjelpemidler.delbestilling.common.Saksbehandlingstype
import no.nav.hjelpemidler.delbestilling.common.Serienr
import no.nav.hjelpemidler.delbestilling.config.isDev
import no.nav.hjelpemidler.delbestilling.config.isLocal
import no.nav.hjelpemidler.delbestilling.config.isProd
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.AnmodningService
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.Anmodningrapport
import no.nav.hjelpemidler.delbestilling.infrastructure.geografi.Kommuneoppslag
import no.nav.hjelpemidler.delbestilling.infrastructure.jsonMapper
import no.nav.hjelpemidler.delbestilling.infrastructure.kafka.ManuellDelbestillingKafkaPayload
import no.nav.hjelpemidler.delbestilling.infrastructure.kafka.SOKNADSBEHANDLING_TOPIC
import no.nav.hjelpemidler.delbestilling.infrastructure.metrics.Metrics
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.OPPRETT_DELBESTILLING_EVENT_NAME
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Oebs
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.byggOebsKafkaPayload
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.Pdl
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.PersonNotAccessibleInPdl
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.PersonNotFoundInPdl
import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction.Transactional
import no.nav.hjelpemidler.delbestilling.infrastructure.roller.Delbestiller
import no.nav.hjelpemidler.delbestilling.infrastructure.roller.Organisasjon
import no.nav.hjelpemidler.delbestilling.infrastructure.slack.Slack
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.data.hmsnr2Hjm
import no.nav.hjelpemidler.delbestilling.pdf.PdfGeneratorClient
import no.nav.hjelpemidler.domain.person.Fødselsnummer
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID


private val log = KotlinLogging.logger {}

class DelbestillingService(
    private val transaction: Transactional,
    private val pdl: Pdl,
    private val oebs: Oebs,
    private val kommuneoppslag: Kommuneoppslag,
    private val metrics: Metrics,
    private val slack: Slack,
    private val anmodningService: AnmodningService,
    private val pdfClient: PdfGeneratorClient,
) {
    suspend fun opprettDelbestilling(
        request: DelbestillingRequest,
        bestillerFnr: String,
        delbestillerRolle: Delbestiller,
    ): DelbestillingResultat {
        val id = request.delbestilling.id
        val hmsnr = request.delbestilling.hmsnr
        val serienr = request.delbestilling.serienr
        val brukernr = request.delbestilling.brukernr
        log.info { "Oppretter delbestilling for hmsnr $hmsnr, serienr $serienr" }
        log.info { "Delbestillerrolle: $delbestillerRolle" }

        val feil = validerDelbestillingRate(bestillerFnr, hmsnr, serienr, brukernr)
        if (feil != null) {
            return DelbestillingResultat(id, feil = feil)
        }

        val brukersFnr =
            hentInnbyggersFnr(hmsnr = hmsnr, serienr = serienr, brukernr = brukernr) ?: return DelbestillingResultat(
                id,
                feil = DelbestillingFeil.INGET_UTLÅN
            )

        val brukerKommunenr = try {
            pdl.hentKommunenummer(brukersFnr)
        } catch (e: PersonNotAccessibleInPdl) {
            log.error(e) { "Person ikke tilgjengelig i PDL" }
            return DelbestillingResultat(id, feil = DelbestillingFeil.KAN_IKKE_BESTILLE)
        } catch (e: PersonNotFoundInPdl) {
            log.error(e) { "Person ikke funnet i PDL" }
            return DelbestillingResultat(id, feil = DelbestillingFeil.BRUKER_IKKE_FUNNET)
        } catch (e: Exception) {
            log.error(e) { "Klarte ikke å hente bruker fra PDL" }
            throw e
        }

        val lagerEnhet = try {
            oebs.finnLagerenhet(brukerKommunenr)
        } catch (e: Exception) {
            log.error(e) { "Klarte ikke opprette delbestilling, fant ikke lagerenhet for kommunenummer $brukerKommunenr" }
            return DelbestillingResultat(id, feil = DelbestillingFeil.LAGERENHET_IKKE_FUNNET)
        }

        val brukersKommunenavn = kommuneoppslag.kommunenavnOrNull(brukerKommunenr) ?: "Ukjent"

        // Det skal ikke være mulig å bestille til seg selv (disabler i dev pga testdata)
        if (isProd() && bestillerFnr == brukersFnr) {
            log.info { "Bestiller prøver å bestille til seg selv" }
            return DelbestillingResultat(id, feil = DelbestillingFeil.BESTILLE_TIL_SEG_SELV)
        }

        // Sjekk at PDL og OEBS kommunenr på bruker stemmer overens
        val oebsBrukerinfo = oebs.hentPersoninfo(brukersFnr)
        val brukerHarSammeKommunenrIOebsOgPdl = oebsBrukerinfo.any { it.leveringKommune == brukerKommunenr }
        if (!isDev() && !brukerHarSammeKommunenrIOebsOgPdl) {
            log.info { "Ulik leveringsadresse. OEBS: $oebsBrukerinfo, PDL: $brukerKommunenr" }
            return DelbestillingResultat(id, feil = DelbestillingFeil.ULIK_ADRESSE_PDL_OEBS)
        }

        // Sjekk om en av innsenders organisasjoner tilhører brukers kommuner
        var innsendersRepresenterteOrganisasjon =
            delbestillerRolle.representasjoner.find { it.kommunenummer == brukerKommunenr }
        val bestillerType: BestillerType =
            if (delbestillerRolle.kommunaleAnsettelsesforhold.any { it.kommunenummer == brukerKommunenr }) BestillerType.KOMMUNAL else BestillerType.IKKE_KOMMUNAL

        if (innsendersRepresenterteOrganisasjon == null) {
            log.info { "Brukers kommunenr: $brukerKommunenr, innsenders rolle: $delbestillerRolle" }
            if (isDev()) {
                innsendersRepresenterteOrganisasjon = Organisasjon("1234", navn = "Testorg for dev")
            } else {
                return DelbestillingResultat(
                    id,
                    feil = DelbestillingFeil.ULIK_GEOGRAFISK_TILKNYTNING,
                )
            }
        }

        val bestillersNavn = pdl.hentFornavn(bestillerFnr)


        return if (request.delbestilling.ukjenteDeler.isEmpty()) {
            log.info { "Innsending av delbestilling med id $id, hmsnr $hmsnr, serienr $serienr, brukernr $brukernr" }
            opprettAutomatiskDelbestilling(
                request,
                brukerKommunenr,
                bestillerFnr,
                brukersFnr,
                brukersKommunenavn,
                innsendersRepresenterteOrganisasjon,
                bestillerType,
                lagerEnhet,
                bestillersNavn,
                id
            )
        } else {
            log.info { "Innsending av delbestilling med id $id, hmsnr $hmsnr, serienr $serienr, brukernr $brukernr. Ukjente deler: ${request.delbestilling.ukjenteDeler}" }
            opprettDelbestillingTilManuellSaksbehandling(
                request,
                brukerKommunenr,
                bestillerFnr,
                brukersFnr,
                brukersKommunenavn,
                innsendersRepresenterteOrganisasjon,
                bestillerType,
                lagerEnhet,
                pdl.hentNavn(bestillerFnr),
                id,
            )
        }
    }

    private suspend fun opprettDelbestillingTilManuellSaksbehandling(
        request: DelbestillingRequest,
        brukerKommunenr: String,
        bestillerFnr: String,
        brukersFnr: String,
        brukersKommunenavn: String,
        innsendersRepresenterteOrganisasjon: Organisasjon,
        bestillerType: BestillerType,
        lagerEnhet: Lager,
        bestillersNavn: String,
        id: UUID
    ): DelbestillingResultat {
        val personNavnOgAdresseTilPDF = pdl.henthentPersonNavnOgAdresse(brukersFnr)
        val delbestilling = request.delbestilling

        val pdf = lagPdf(personNavnOgAdresseTilPDF, brukersFnr, delbestilling, bestillersNavn)
        val delbestillingSak = transaction(returnGeneratedKeys = true) {

            log.info { "Lagrer manuell delbestilling '${delbestilling.id}'" }
            val saksnummer = delbestillingRepository.lagreDelbestilling(
                bestillerFnr,
                brukersFnr,
                brukerKommunenr,
                delbestilling,
                brukersKommunenavn,
                innsendersRepresenterteOrganisasjon,
                bestillerType,
                lagerEnhet,
                saksbehandlingstype = Saksbehandlingstype.MANUELL,
                pdfTilManuellBestilling = pdf,
            )

            // Hent ut den nye delbestillingsaken
            val nyDelbestillingSak = delbestillingRepository.hentDelbestilling(saksnummer)
                ?: throw RuntimeException("Klarte ikke hente ut delbestillingsak for saksnummer $saksnummer")

            // Skriv Kafka-event til outbox atomisk med delbestillingen for bestillinger som skal manuelt behandles.
            val payload = ManuellDelbestillingKafkaPayload(
                eventId = UUID.randomUUID(),
                saksnummer = saksnummer,
                brukersFnr = brukersFnr,
                mottattTidspunkt = LocalDateTime.now(),
            )
            outboxDao.leggTil(
                topic = SOKNADSBEHANDLING_TOPIC,
                key = payload.saksnummer.toString(),
                eventName = payload.eventName,
                eventId = payload.eventId,
                payload = jsonMapper.writeValueAsString(payload),
            )

            if (isDev()) {
                log.info { "Manuell delbestilling lagt til outbox: $payload" }
            }

            nyDelbestillingSak
        }

        log.info { "Manuell delbestilling '$id' sendt inn med saksnummer '${delbestillingSak.saksnummer}'" }

        sendStatistikk(request.delbestilling, brukersFnr)

        if (!isLocal()) {
            slack.varsleOmInnsending(brukerKommunenr, brukersKommunenavn)
        }

        return DelbestillingResultat(
            id, null, delbestillingSak.saksnummer, delbestillingSak
        )
    }

    private suspend fun opprettAutomatiskDelbestilling(
        request: DelbestillingRequest,
        brukerKommunenr: String,
        bestillerFnr: String,
        brukersFnr: String,
        brukersKommunenavn: String,
        innsendersRepresenterteOrganisasjon: Organisasjon,
        bestillerType: BestillerType,
        lagerEnhet: Lager,
        bestillersNavn: String,
        id: UUID
    ): DelbestillingResultat {
        val delerHmsnr = request.delbestilling.deler.map { it.del.hmsnr }
        val lagerstatuser = oebs.hentLagerstatusForKommunenummer(brukerKommunenr, delerHmsnr)
        val berikedeDellinjer = request.delbestilling.deler.map { dellinje ->
            val lagerstatus =
                checkNotNull(lagerstatuser.find { it.artikkelnummer == dellinje.del.hmsnr }) { "Mangler lagerstatus for ${dellinje.del.hmsnr}" }
            dellinje.copy(lagerstatusPåBestillingstidspunkt = lagerstatus) // Brukes senere i AnmodningService for å finne ut om det er behov for anmodning.
        }
        val delbestilling = request.delbestilling.copy(deler = berikedeDellinjer)

        val delbestillingSak = transaction(returnGeneratedKeys = true) {
            log.info { "Lagrer delbestilling '${delbestilling.id}'" }
            val saksnummer = delbestillingRepository.lagreDelbestilling(
                bestillerFnr,
                brukersFnr,
                brukerKommunenr,
                delbestilling,
                brukersKommunenavn,
                innsendersRepresenterteOrganisasjon,
                bestillerType,
                lagerEnhet,
                saksbehandlingstype = Saksbehandlingstype.AUTOMATISK
            )

            // Hent ut den nye delbestillingsaken
            val nyDelbestillingSak = delbestillingRepository.hentDelbestilling(saksnummer)
                ?: throw RuntimeException("Klarte ikke hente ut delbestillingsak for saksnummer $saksnummer")

            anmodningService.lagreDelerUtenDekning(nyDelbestillingSak)

            // Skriv Kafka-event til outbox atomisk med delbestillingen
            val ordre = oebs.byggOrdre(nyDelbestillingSak, Fødselsnummer(brukersFnr), bestillersNavn)
            val eventId = UUID.randomUUID()
            outboxDao.leggTil(
                topic = SOKNADSBEHANDLING_TOPIC,
                key = nyDelbestillingSak.saksnummer.toString(),
                eventName = OPPRETT_DELBESTILLING_EVENT_NAME,
                eventId = eventId,
                payload = byggOebsKafkaPayload(eventId, ordre),
            )

            nyDelbestillingSak
        }

        log.info { "Delbestilling '$id' sendt inn med saksnummer '${delbestillingSak.saksnummer}'" }

        sendStatistikk(request.delbestilling, brukersFnr)

        if (!isLocal()) {
            slack.varsleOmInnsending(brukerKommunenr, brukersKommunenavn)
        }

        return DelbestillingResultat(id, null, delbestillingSak.saksnummer, delbestillingSak)
    }

    suspend fun hentInnbyggersFnr(hmsnr: String, serienr: Serienr?, brukernr: String?): String? {
        return if (serienr != null) oebs.hentFnrLeietakerFraSerienr(hmsnr, serienr)
        else if (brukernr != null) oebs.hentFnr(brukernr)
        else null
    }

    suspend fun sendStatistikk(delbestilling: Delbestilling, fnrBruker: String) = coroutineScope {
        launch {
            try {
                val navnHovedprodukt = hmsnr2Hjm[delbestilling.hmsnr]?.navn ?: "Ukjent"
                val hjmbrukerHarBrukerpass = oebs.harBrukerpass(fnrBruker)
                delbestilling.deler.forEach {
                    metrics.registrerDelbestillingInnsendt(
                        del = it.del,
                        hmsnrHovedprodukt = delbestilling.hmsnr,
                        navnHovedprodukt = navnHovedprodukt,
                        rolleInnsender = "Tekniker",
                        hjmbrukerHarBrukerpass = hjmbrukerHarBrukerpass,
                    )
                }

                delbestilling.ukjenteDeler.forEach {
                    metrics.registrerDelbestillingInnsendtUkjenteDeler(
                        del = it.delUkjent,
                        hmsnrHovedprodukt = delbestilling.hmsnr,
                        navnHovedprodukt = navnHovedprodukt,
                        rolleInnsender = "Tekniker",
                        hjmbrukerHarBrukerpass = hjmbrukerHarBrukerpass,
                    )
                }


            } catch (t: Throwable) {
                log.error(t) { "Lagring av statistikk om innsendt delbestilling feilet" }
            }
        }
    }

    private suspend fun validerDelbestillingRate(
        bestillerFnr: String,
        hmsnr: String,
        serienr: String?,
        brukernr: String?,
    ): DelbestillingFeil? {
        if (isDev()) {
            return null // For enklere testing i dev
        }
        val maxAntallBestillingerPer24Timer = 5
        val tidspunkt24TimerSiden = LocalDateTime.now().minusDays(1)
        val bestillersBestillinger =
            hentDelbestillinger(bestillerFnr).filter { it.opprettet.isAfter(tidspunkt24TimerSiden) }
                .filter { it.delbestilling.hmsnr == hmsnr && (it.delbestilling.serienr == serienr || it.delbestilling.brukernr == brukernr) } // TOOO test
        if (bestillersBestillinger.size >= maxAntallBestillingerPer24Timer) {
            log.info { "Tekniker har nådd grensen på $maxAntallBestillingerPer24Timer bestillinger siste 24 timer for hjelpemiddel hmsnr:$hmsnr serienr:$serienr" }
            return DelbestillingFeil.FOR_MANGE_BESTILLINGER_SISTE_24_TIMER
        }
        return null
    }

    suspend fun hentDelbestillinger(bestillerFnr: String): List<DelbestillingSak> = transaction {
        delbestillingRepository.hentDelbestillinger(bestillerFnr)
    }

    suspend fun sjekkXKLager(hmsnr: Hmsnr, serienr: Serienr?, brukernr: String?): Boolean {
        val brukersFnr =
            hentInnbyggersFnr(hmsnr, serienr, brukernr) ?: error("Fant ikke fnr for hmsnr=$hmsnr, serienr=$serienr")
        val kommunenummer = pdl.hentKommunenummer(brukersFnr)
        return harXKLager(kommunenummer)
    }

    suspend fun rapporterDelerTilAnmodning(): List<Anmodningrapport> {
        return try {
            val rapporter = anmodningService.genererAnmodningsrapporter()

            rapporter.forEach { rapport ->
                if (rapport.anmodningsbehov.isNotEmpty()) {
                    transaction {
                        delUtenDekningDao.markerDelerSomBehandlet(
                            rapport.lager, rapport.anmodningsbehov.map { it.hmsnr })
                        anmodningDao.lagreAnmodninger(rapport)
                        anmodningService.sendAnmodningRapport(rapport)
                    }
                } else {
                    log.info { "Anmodningsbehov for enhet ${rapport.lager} er tomt, alle deler har dermed fått dekning etter innsending. Hopper over." }
                }

                if (rapport.delerSomIkkeLengerMåAnmodes.isNotEmpty()) {
                    transaction {
                        delUtenDekningDao.markerDelerSomBehandlet(
                            rapport.lager, rapport.delerSomIkkeLengerMåAnmodes.map { it.hmsnr })
                    }
                    slack.varsleOmEtterfyllingHosEnhet(rapport.lager, rapport.delerSomIkkeLengerMåAnmodes)
                }


            }

            if (rapporter.isEmpty() || rapporter.all { it.anmodningsbehov.isEmpty() }) {
                slack.varsleOmIngenAnmodninger()
            }

            rapporter
        } catch (t: Throwable) {
            log.error(t) { "Rapportering av nødvendige anmodninger feilet." }
            slack.varsleOmRapporteringFeilet()
            throw t
        }
    }

    private suspend fun lagPdf(
        personNavnOgAdresseTilPDF: PersonNavnOgAdresse,
        brukersFnr: String,
        delbestilling: Delbestilling,
        bestillersNavn: String
    ) : ByteArray{
        val delbestillingTilPdf = genererPdfTilManuellSaksbehandler(
            personNavnOgAdresseTilPDF,
            brukersFnr,
            delbestilling,
            bestillersNavn
        )
        return pdfClient.lagDelbestillingsbrev(delbestillingTilPdf)
    }

    private fun genererPdfTilManuellSaksbehandler(
        personNavnOgAdresseTilPDF: PersonNavnOgAdresse,
        brukersFnr: String,
        delbestilling: Delbestilling,
        bestillersNavn: String
    ): DelbestillingTilPdf {
        val delbestillingTilPdf = DelbestillingTilPdf(
            mottattDato = LocalDate.now(),
            navnBruker = personNavnOgAdresseTilPDF.navn,
            fnrBruker = brukersFnr,
            adresseBruker = personNavnOgAdresseTilPDF.adresse,
            brukernummer = delbestilling.brukernr,
            hjelpemiddelnavn = delbestilling.navn,
            hjelpemiddelserienr = delbestilling.serienr,
            hjelpemiddelHmsnr = delbestilling.hmsnr,
            navnTekniker = bestillersNavn,
            beskjed517 = if (delbestilling.levering == Levering.TIL_XK_LAGER) "XK-Lager " else "",
            leveringsadresse = "Kommunalt Mottakssted", // TODO: Bekreft at dette skal stå som standard.
            deler = delbestilling.deler.map { delLinje ->
                Del(
                    hmsnr = delLinje.del.hmsnr,
                    navn = delLinje.del.navn,
                    antall = delLinje.antall
                )
            },
            ukjenteDeler = delbestilling.ukjenteDeler.map { ukjentDel ->
                UkjentDel(
                    hmsnr = ukjentDel.delUkjent.hmsnr,
                    levArtnr = ukjentDel.delUkjent.levArtnr,
                    antall = ukjentDel.antall
                )
            },
            totalAntallDeler = delbestilling.deler.sumOf { it.antall } + delbestilling.ukjenteDeler.sumOf { it.antall }
        )
        return delbestillingTilPdf
    }

    suspend fun hentPdf(saksnummer: Long): ByteArray {
        return transaction{
            delbestillingRepository.hentPdf(saksnummer)
        }
    }
}
