package no.nav.hjelpemidler.delbestilling.delbestilling

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import no.nav.hjelpemidler.delbestilling.common.Delbestilling
import no.nav.hjelpemidler.delbestilling.common.DelbestillingSak
import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.common.Lager
import no.nav.hjelpemidler.delbestilling.common.Saksbehandlingstype
import no.nav.hjelpemidler.delbestilling.common.Serienr
import no.nav.hjelpemidler.delbestilling.config.isLocal
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.AnmodningService
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.Anmodningrapport
import no.nav.hjelpemidler.delbestilling.infrastructure.geografi.Geografioppslag
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
import no.nav.hjelpemidler.delbestilling.infrastructure.slack.Slack
import no.nav.hjelpemidler.domain.person.Fødselsnummer
import java.time.LocalDateTime
import java.util.UUID


private val log = KotlinLogging.logger {}

class DelbestillingService(
    private val transaction: Transactional,
    private val pdl: Pdl,
    private val oebs: Oebs,
    private val geografioppslag: Geografioppslag,
    private val metrics: Metrics,
    private val slack: Slack,
    private val anmodningService: AnmodningService,
    private val regler: DelbestillingRegler,
) {
    suspend fun opprettDelbestilling(
        request: DelbestillingRequest,
        bestillerFnr: String,
        delbestillerRolle: Delbestiller,
    ): DelbestillingResultat {
        val delbestilling = request.delbestilling
        val id = delbestilling.id
        val hmsnr = delbestilling.hmsnr
        val serienr = delbestilling.serienr
        val brukernr = delbestilling.brukernr
        log.info { "Oppretter delbestilling for hmsnr $hmsnr, serienr $serienr, brukernr $brukernr" }
        log.info { "Delbestillerrolle: $delbestillerRolle" }

        validerDelbestillingRate(bestillerFnr, hmsnr, serienr, brukernr)?.let { feil ->
            return DelbestillingResultat(id, feil = feil)
        }

        val bruker = hentBruker(delbestilling).getOrElse { feil ->
            return DelbestillingResultat(id, feil = feil)
        }

        val innsending = lagInnsending(bestillerFnr, delbestillerRolle, bruker).getOrElse { feil ->
            return DelbestillingResultat(id, feil = feil)
        }

        return when (delbestilling.saksbehandlingstype()) {
            Saksbehandlingstype.AUTOMATISK -> {
                log.info { "Innsending av delbestilling med id $id, hmsnr $hmsnr, serienr $serienr, brukernr $brukernr" }
                opprettAutomatiskDelbestilling(delbestilling, innsending)
            }

            Saksbehandlingstype.MANUELL -> {
                log.info { "Innsending av delbestilling med id $id, hmsnr $hmsnr, serienr $serienr, brukernr $brukernr. Ukjente deler: ${delbestilling.ukjenteDeler}" }
                opprettDelbestillingTilManuellSaksbehandling(delbestilling, innsending)
            }
        }
    }

    private suspend fun hentBruker(delbestilling: Delbestilling): Utfall<Bruker> {
        val fnr = hentInnbyggersFnr(
            hmsnr = delbestilling.hmsnr,
            serienr = delbestilling.serienr,
            brukernr = delbestilling.brukernr,
        ) ?: return Utfall.Feil(DelbestillingFeil.INGET_UTLÅN)

        val kommunenr = try {
            pdl.hentKommunenummer(fnr)
        } catch (e: PersonNotAccessibleInPdl) {
            log.error(e) { "Person ikke tilgjengelig i PDL" }
            return Utfall.Feil(DelbestillingFeil.KAN_IKKE_BESTILLE)
        } catch (e: PersonNotFoundInPdl) {
            log.error(e) { "Person ikke funnet i PDL" }
            return Utfall.Feil(DelbestillingFeil.BRUKER_IKKE_FUNNET)
        } catch (e: Exception) {
            log.error(e) { "Klarte ikke å hente bruker fra PDL" }
            throw e
        }

        val lagerEnhet = try {
            oebs.finnLagerenhet(kommunenr)
        } catch (e: Exception) {
            log.error(e) { "Klarte ikke opprette delbestilling, fant ikke lagerenhet for kommunenummer $kommunenr" }
            return Utfall.Feil(DelbestillingFeil.LAGERENHET_IKKE_FUNNET)
        }

        val kommunenavn = geografioppslag.kommunenavnOrNull(kommunenr) ?: "Ukjent"

        return Utfall.Ok(Bruker(fnr, kommunenr, kommunenavn, lagerEnhet))
    }

    private suspend fun lagInnsending(
        bestillerFnr: String,
        delbestillerRolle: Delbestiller,
        bruker: Bruker,
    ): Utfall<Innsending> {
        if (regler.sjekkBestillingTilSegSelv && bestillerFnr == bruker.fnr) {
            log.info { "Bestiller prøver å bestille til seg selv" }
            return Utfall.Feil(DelbestillingFeil.BESTILLE_TIL_SEG_SELV)
        }

        if (regler.sjekkKommunenrPdlOebs) {
            val oebsBrukerinfo = oebs.hentPersoninfo(bruker.fnr)
            if (oebsBrukerinfo.none { it.leveringKommune == bruker.kommunenr }) {
                log.info { "Ulik leveringsadresse. OEBS: $oebsBrukerinfo, PDL: ${bruker.kommunenr}" }
                return Utfall.Feil(DelbestillingFeil.ULIK_ADRESSE_PDL_OEBS)
            }
        }

        // Sjekk om en av innsenders organisasjoner tilhører brukers kommune
        val innsendersRepresenterteOrganisasjon =
            delbestillerRolle.representasjoner.find { it.kommunenummer == bruker.kommunenr }
                ?: run {
                    log.info { "Brukers kommunenr: ${bruker.kommunenr}, innsenders rolle: $delbestillerRolle" }
                    return Utfall.Feil(DelbestillingFeil.ULIK_GEOGRAFISK_TILKNYTNING)
                }

        val bestillerType =
            if (delbestillerRolle.kommunaleAnsettelsesforhold.any { it.kommunenummer == bruker.kommunenr }) BestillerType.KOMMUNAL else BestillerType.IKKE_KOMMUNAL

        return Utfall.Ok(
            Innsending(
                bestillerFnr = bestillerFnr,
                brukersFnr = bruker.fnr,
                brukerKommunenr = bruker.kommunenr,
                brukersKommunenavn = bruker.kommunenavn,
                innsendersRepresenterteOrganisasjon = innsendersRepresenterteOrganisasjon,
                bestillerType = bestillerType,
                lagerEnhet = bruker.lagerEnhet,
            )
        )
    }

    private suspend fun opprettDelbestillingTilManuellSaksbehandling(
        delbestilling: Delbestilling,
        innsending: Innsending,
    ): DelbestillingResultat {
        val bestillerNavn = try {
            pdl.hentNavn(innsending.bestillerFnr)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            log.warn(e) { "Klarte ikke hente bestillers navn for manuell delbestilling '${delbestilling.id}', sender epost uten navn" }
            null
        }

        val delbestillingSak = transaction(returnGeneratedKeys = true) {

            log.info { "Lagrer manuell delbestilling '${delbestilling.id}'" }
            val saksnummer = delbestillingRepository.lagreDelbestilling(delbestilling, innsending)

            // Hent ut den nye delbestillingsaken
            val nyDelbestillingSak = delbestillingRepository.hentDelbestilling(saksnummer)
                ?: throw RuntimeException("Klarte ikke hente ut delbestillingsak for saksnummer $saksnummer")

            val epost = ManuellDelbestillingEpost(delbestilling, saksnummer, bestillerNavn)
            epostOutboxDao.leggTil(
                innsending.lagerEnhet.epostForManuellDelbestilling(),
                MANUELL_DELBESTILLING_EPOST_EMNE,
                epost.tilHtml())

            log.info { "Epost med manuell delbestilling for saksnr. $saksnummer sendt til ${innsending.lagerEnhet.epostForManuellDelbestilling()}" }

            delbestilling.epostTekniker?.trim()?.takeIf { it.isNotBlank() }?.let { bestillerepost ->
                bestillerepostDao.lagre(innsending.bestillerFnr, bestillerepost)
            }

            nyDelbestillingSak
        }

        log.info { "Manuell delbestilling '${delbestilling.id}' sendt inn med saksnummer '${delbestillingSak.saksnummer}'" }

        sendStatistikk(delbestilling, innsending.brukersFnr)

        if (!isLocal()) {
            slack.varsleOmInnsending(innsending.brukerKommunenr, innsending.brukersKommunenavn)
        }

        return DelbestillingResultat(
            delbestilling.id, null, delbestillingSak.saksnummer, delbestillingSak
        )
    }

    private suspend fun opprettAutomatiskDelbestilling(
        delbestilling: Delbestilling,
        innsending: Innsending,
    ): DelbestillingResultat {
        val bestillersNavn = pdl.hentFornavn(innsending.bestillerFnr)
        val delerHmsnr = delbestilling.deler.map { it.del.hmsnr }
        val lagerstatuser = oebs.hentLagerstatusForKommunenummer(innsending.brukerKommunenr, delerHmsnr)
        val berikedeDellinjer = delbestilling.deler.map { dellinje ->
            val lagerstatus =
                checkNotNull(lagerstatuser.find { it.artikkelnummer == dellinje.del.hmsnr }) { "Mangler lagerstatus for ${dellinje.del.hmsnr}" }
            dellinje.copy(lagerstatusPåBestillingstidspunkt = lagerstatus) // Brukes senere i AnmodningService for å finne ut om det er behov for anmodning.
        }
        val berikedDelbestilling = delbestilling.copy(deler = berikedeDellinjer)

        val delbestillingSak = transaction(returnGeneratedKeys = true) {
            log.info { "Lagrer delbestilling '${berikedDelbestilling.id}'" }
            val saksnummer = delbestillingRepository.lagreDelbestilling(berikedDelbestilling, innsending)

            // Hent ut den nye delbestillingsaken
            val nyDelbestillingSak = delbestillingRepository.hentDelbestilling(saksnummer)
                ?: throw RuntimeException("Klarte ikke hente ut delbestillingsak for saksnummer $saksnummer")

            anmodningService.lagreDelerUtenDekning(nyDelbestillingSak)

            // Skriv Kafka-event til outbox atomisk med delbestillingen
            val ordre = oebs.byggOrdre(nyDelbestillingSak, Fødselsnummer(innsending.brukersFnr), bestillersNavn)
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

        log.info { "Delbestilling '${delbestilling.id}' sendt inn med saksnummer '${delbestillingSak.saksnummer}'" }

        sendStatistikk(delbestilling, innsending.brukersFnr)

        if (!isLocal()) {
            slack.varsleOmInnsending(innsending.brukerKommunenr, innsending.brukersKommunenavn)
        }

        return DelbestillingResultat(delbestilling.id, null, delbestillingSak.saksnummer, delbestillingSak)
    }

    suspend fun hentInnbyggersFnr(hmsnr: String, serienr: Serienr?, brukernr: String?): String? {
        return if (!serienr.isNullOrBlank()) oebs.hentFnrLeietakerFraSerienr(hmsnr, serienr)
        else if (!brukernr.isNullOrBlank()) oebs.hentUtlånPåArtNrOgBrukernr(hmsnr, brukernr).firstOrNull()?.fnr
        else null
    }

    suspend fun sendStatistikk(delbestilling: Delbestilling, fnrBruker: String) = coroutineScope {
        launch {
            try {
                val navnHovedprodukt = delbestilling.navn ?: "Ukjent"
                val hjmbrukerHarBrukerpass = oebs.harBrukerpass(fnrBruker)
                delbestilling.deler.forEach {
                    metrics.registrerDelbestillingInnsendtKjenteDeler(
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

                metrics.registrerDelbestillingInnsendtSak(
                    hmsnrHovedprodukt = delbestilling.hmsnr,
                    navnHovedprodukt = navnHovedprodukt,
                    rolleInnsender = "Tekniker",
                    hjmbrukerHarBrukerpass = hjmbrukerHarBrukerpass,
                    saksbehandlingstype = delbestilling.saksbehandlingstype(),
                    serienr = delbestilling.serienr,
                    brukernr = delbestilling.brukernr,
                )


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
        if (!regler.sjekkBestillingsrate) {
            return null
        }
        val maxAntallBestillingerPer24Timer = 5
        val tidspunkt24TimerSiden = LocalDateTime.now().minusDays(1)
        val bestillersBestillinger =
            hentDelbestillinger(bestillerFnr).filter { it.opprettet.isAfter(tidspunkt24TimerSiden) }
                .filter {
                    it.delbestilling.hmsnr == hmsnr &&
                        harSammeBestillingsidentifikator(it.delbestilling, serienr, brukernr)
                }
        if (bestillersBestillinger.size >= maxAntallBestillingerPer24Timer) {
            log.info { "Tekniker har nådd grensen på $maxAntallBestillingerPer24Timer bestillinger siste 24 timer for hjelpemiddel hmsnr:$hmsnr serienr:$serienr" }
            return DelbestillingFeil.FOR_MANGE_BESTILLINGER_SISTE_24_TIMER
        }
        return null
    }

    suspend fun hentDelbestillinger(bestillerFnr: String): List<DelbestillingSak> = transaction {
        delbestillingRepository.hentDelbestillinger(bestillerFnr)
    }

    suspend fun hentSisteBestillerepost(bestillerFnr: String): String? = transaction {
        bestillerepostDao.hent(bestillerFnr)
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

}

private data class Bruker(
    val fnr: String,
    val kommunenr: String,
    val kommunenavn: String,
    val lagerEnhet: Lager,
)

private sealed interface Utfall<out T> {
    data class Ok<T>(val verdi: T) : Utfall<T>
    data class Feil(val feil: DelbestillingFeil) : Utfall<Nothing>
}

private inline fun <T> Utfall<T>.getOrElse(vedFeil: (DelbestillingFeil) -> Nothing): T = when (this) {
    is Utfall.Ok -> verdi
    is Utfall.Feil -> vedFeil(feil)
}

internal fun harSammeBestillingsidentifikator(
    delbestilling: Delbestilling,
    serienr: Serienr?,
    brukernr: String?,
): Boolean = when {
    serienr != null -> delbestilling.serienr == serienr
    brukernr != null -> delbestilling.brukernr == brukernr
    else -> false
}
