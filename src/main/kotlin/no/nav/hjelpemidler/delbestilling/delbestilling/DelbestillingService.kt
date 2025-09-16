package no.nav.hjelpemidler.delbestilling.delbestilling

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import no.nav.hjelpemidler.delbestilling.common.Delbestilling
import no.nav.hjelpemidler.delbestilling.common.DelbestillingSak
import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.common.Serienr
import no.nav.hjelpemidler.delbestilling.config.isDev
import no.nav.hjelpemidler.delbestilling.config.isLocal
import no.nav.hjelpemidler.delbestilling.config.isProd
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.AnmodningService
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.Anmodningrapport
import no.nav.hjelpemidler.delbestilling.infrastructure.geografi.Kommuneoppslag
import no.nav.hjelpemidler.delbestilling.infrastructure.metrics.Metrics
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Oebs
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.Pdl
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.PersonNotAccessibleInPdl
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.PersonNotFoundInPdl
import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction.Transactional
import no.nav.hjelpemidler.delbestilling.infrastructure.roller.Delbestiller
import no.nav.hjelpemidler.delbestilling.infrastructure.roller.Organisasjon
import no.nav.hjelpemidler.delbestilling.infrastructure.slack.Slack
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.data.hmsnr2Hjm
import no.nav.hjelpemidler.domain.person.Fødselsnummer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


private val log = KotlinLogging.logger {}

class DelbestillingService(
    private val transaction: Transactional,
    private val pdl: Pdl,
    private val oebs: Oebs,
    private val kommuneoppslag: Kommuneoppslag,
    private val metrics: Metrics,
    private val slack: Slack,
    private val anmodningService: AnmodningService,
) {
    suspend fun opprettDelbestilling(
        request: DelbestillingRequest,
        bestillerFnr: String,
        delbestillerRolle: Delbestiller,
    ): DelbestillingResultat {
        val id = request.delbestilling.id
        val hmsnr = request.delbestilling.hmsnr
        val serienr = request.delbestilling.serienr
        log.info { "Oppretter delbestilling for hmsnr $hmsnr, serienr $serienr" }
        log.info { "Delbestillerrolle: $delbestillerRolle" }

        val feil = validerDelbestillingRate(bestillerFnr, hmsnr, serienr)
        if (feil != null) {
            return DelbestillingResultat(id, feil = feil)
        }

        val brukersFnr = oebs.hentFnrLeietaker(hmsnr, serienr)
            ?: return DelbestillingResultat(id, feil = DelbestillingFeil.INGET_UTLÅN)

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

        // TODO rydd og splitt ut logikk i egne klasser etc.
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
            )

            // Hent ut den nye delbestillingsaken
            val nyDelbestillingSak = delbestillingRepository.hentDelbestilling(saksnummer)
                ?: throw RuntimeException("Klarte ikke hente ut delbestillingsak for saksnummer $saksnummer")

            anmodningService.lagreDelerUtenDekning(nyDelbestillingSak)

            oebs.sendDelbestilling(nyDelbestillingSak, Fødselsnummer(brukersFnr), bestillersNavn)

            nyDelbestillingSak
        }

        log.info { "Delbestilling '$id' sendt inn med saksnummer '${delbestillingSak.saksnummer}'" }

        sendStatistikk(request.delbestilling, brukersFnr)

        if (!isLocal()) {
            slack.varsleOmInnsending(brukerKommunenr, brukersKommunenavn)
            val idag = LocalDate.now()
            lagRapport(fra = idag.minusDays(7), til = idag)
        }

        return DelbestillingResultat(id, null, delbestillingSak.saksnummer, delbestillingSak)
    }

    suspend fun sendStatistikk(delbestilling: Delbestilling, fnrBruker: String) = coroutineScope {
        launch {
            try {
                val navnHovedprodukt = hmsnr2Hjm[delbestilling.hmsnr]?.navn ?: "Ukjent"
                val hjmbrukerHarBrukerpass = oebs.harBrukerpass(fnrBruker)
                delbestilling.deler.forEach {
                    metrics.registrerDelbestillingInnsendt(
                        hmsnrDel = it.del.hmsnr,
                        navnDel = it.del.navn,
                        hmsnrHovedprodukt = delbestilling.hmsnr,
                        navnHovedprodukt = navnHovedprodukt,
                        rolleInnsender = "Tekniker",
                        hjmbrukerHarBrukerpass = hjmbrukerHarBrukerpass
                    )
                }
            } catch (t: Throwable) {
                log.error(t) { "Lagring av statistikk om innsendt delbestilling feilet" }
            }
        }
    }

    private suspend fun validerDelbestillingRate(bestillerFnr: String, hmsnr: String, serienr: String): DelbestillingFeil? {
        if (isDev()) {
            return null // For enklere testing i dev
        }
        val maxAntallBestillingerPer24Timer = 5
        val tidspunkt24TimerSiden = LocalDateTime.now().minusDays(1)
        val bestillersBestillinger = hentDelbestillinger(bestillerFnr)
            .filter { it.opprettet.isAfter(tidspunkt24TimerSiden) }
            .filter { it.delbestilling.hmsnr == hmsnr && it.delbestilling.serienr == serienr }
        if (bestillersBestillinger.size >= maxAntallBestillingerPer24Timer) {
            log.info { "Tekniker har nådd grensen på $maxAntallBestillingerPer24Timer bestillinger siste 24 timer for hjelpemiddel hmsnr:$hmsnr serienr:$serienr" }
            return DelbestillingFeil.FOR_MANGE_BESTILLINGER_SISTE_24_TIMER
        }
        return null
    }

    suspend fun hentDelbestillinger(bestillerFnr: String): List<DelbestillingSak> = transaction {
        delbestillingRepository.hentDelbestillinger(bestillerFnr)
    }

    suspend fun finnTestpersonMedTestbartUtlån(): Map<String, String> {
        val fnrCache = mutableSetOf<String>()
        hmsnr2Hjm.keys.forEach { artnr ->
            log.info { "Leter etter testpersoner med utlån på $artnr" }
            val utlån = oebs.hentUtlånPåArtnr(artnr)
            utlån.forEach { (fnr, artnr, serienr, utlånsDato) ->
                try {
                    if (fnr !in fnrCache) {
                        val kommunenr = pdl.hentKommunenummer(fnr)
                        log.info { "Fant testperson $fnr med utlån på $artnr, $serienr i kommune $kommunenr" }
                        return mapOf("fnr" to fnr, "artnr" to artnr, "serienr" to serienr, "kommunenr" to kommunenr)
                    }
                } catch (e: Exception) {
                    // Peronen finnes ikke i PDL. Ignorer og let videre.
                    log.info(e) { "Ignorer PDL feil under scanning etter testperson" }
                    fnrCache.add(fnr)
                }
            }
        }
        return mapOf("error" to "Ingen testperson funnet")
    }

    suspend fun sjekkXKLager(hmsnr: Hmsnr, serienr: Serienr): Boolean {
        val brukersFnr = oebs.hentFnrLeietaker(artnr = hmsnr, serienr = serienr)
            ?: error("Fant ikke utlån for $hmsnr $serienr")
        val kommunenummer = pdl.hentKommunenummer(brukersFnr)
        return harXKLager(kommunenummer)
    }

    suspend fun rapporterDelerTilAnmodning(): List<Anmodningrapport> {
        return try {
            val rapporter = anmodningService.genererAnmodningsrapporter()

            rapporter.forEach { rapport ->
                if (rapport.anmodningsbehov.isNotEmpty()) {
                    val melding = anmodningService.sendAnmodningRapport(rapport)
                    slack.varsleOmAnmodningrapportSomErSendtTilEnhet(rapport.lager, melding)
                } else {
                    log.info { "Anmodningsbehov for enhet ${rapport.lager} er tomt, alle deler har dermed fått dekning etter innsending. Hopper over." }
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

    // TODO: litt fort og gæli, mye av dette kunne nok heller blitt gjort med databasespørringer
    suspend fun lagRapport(fra: LocalDate, til: LocalDate): String = transaction {
        log.info { "Lager rapport i periode $fra - $til" }
        val alleDelbestillinger = delbestillingRepository.hentDelbestillinger()

        // Nye delbestillinger i periode
        val delbestillingerIPeriode = alleDelbestillinger
            .filter { it.opprettet.toLocalDate() >= fra && it.opprettet.toLocalDate() <= til }

        // Nye kommuner som har sendt inn for første gang i periode
        val nyeKommuner = delbestillingerIPeriode
            .groupBy { it.brukersKommunenavn }
            .mapNotNull { (kommune, delbestillinger) ->
                delbestillinger.minByOrNull { it.opprettet.toLocalDate() }
                    ?.takeIf { it.opprettet.toLocalDate() >= fra && it.opprettet.toLocalDate() <= til }
                    ?.brukersKommunenavn
            }
            .distinct()

        // Prosentvis forskjell i antall delbestillinger siden fjoråret
        val fjoråretsDelbestillinger = alleDelbestillinger.filter {
            it.opprettet.toLocalDate().year == fra.minusYears(1).year
        }

        val åretsDelbestillinger = alleDelbestillinger.filter {
            it.opprettet.toLocalDate().year == fra.year
        }

        val diffFraFjorår = (åretsDelbestillinger.size - fjoråretsDelbestillinger.size) / fjoråretsDelbestillinger.size * 100

        log.info { "delbestillingerIPeriode $fra - ${til}: ${delbestillingerIPeriode.size}" }
        log.info { "nyeKommuner: $nyeKommuner" }
        log.info { "fjoråretsDelbestillinger.size: ${fjoråretsDelbestillinger.size}" }
        log.info { "åretsDelbestillinger.size: ${åretsDelbestillinger.size}" }
        log.info { "diffFraFjorÅr: $diffFraFjorår" }

        val kortFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val rapport = """
            *Rapport for periode ${fra.format(kortFormatter)} - ${til.format(kortFormatter)}* :chart_with_upwards_trend:
            - Antall nye delbestillinger: ${delbestillingerIPeriode.size}
            - Nye kommuner som har sendt inn delbestilling: ${nyeKommuner.joinToString(", ").let { if (it.isEmpty()) "0" }}
            - Diff fra fjorår: ${diffFraFjorår}%
        """.trimIndent()

        slack.sendRapport(rapport)

        return@transaction "Rapport sent"
    }
}
