package no.nav.hjelpemidler.delbestilling.delbestilling

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import no.bekk.bekkopen.date.NorwegianDateUtil
import no.nav.hjelpemidler.delbestilling.config.isDev
import no.nav.hjelpemidler.delbestilling.config.isLocal
import no.nav.hjelpemidler.delbestilling.config.isProd
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.AnmodningService
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.Anmodningrapport
import no.nav.hjelpemidler.delbestilling.delbestilling.model.BestillerType
import no.nav.hjelpemidler.delbestilling.delbestilling.model.Delbestilling
import no.nav.hjelpemidler.delbestilling.delbestilling.model.DelbestillingFeil
import no.nav.hjelpemidler.delbestilling.delbestilling.model.DelbestillingRequest
import no.nav.hjelpemidler.delbestilling.delbestilling.model.DelbestillingResultat
import no.nav.hjelpemidler.delbestilling.delbestilling.model.DelbestillingSak
import no.nav.hjelpemidler.delbestilling.delbestilling.model.DellinjeStatus
import no.nav.hjelpemidler.delbestilling.delbestilling.model.Hmsnr
import no.nav.hjelpemidler.delbestilling.delbestilling.model.Serienr
import no.nav.hjelpemidler.delbestilling.delbestilling.model.Status
import no.nav.hjelpemidler.delbestilling.infrastructure.geografi.Kommuneoppslag
import no.nav.hjelpemidler.delbestilling.infrastructure.metrics.Metrics
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Oebs
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.Pdl
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.PersonNotAccessibleInPdl
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.PersonNotFoundInPdl
import no.nav.hjelpemidler.delbestilling.infrastructure.roller.Delbestiller
import no.nav.hjelpemidler.delbestilling.infrastructure.roller.Organisasjon
import no.nav.hjelpemidler.delbestilling.infrastructure.slack.Slack
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.data.hmsnr2Hjm
import no.nav.hjelpemidler.domain.person.Fødselsnummer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date


private const val LEVERINGSDAGER_FRA_SKIPNINGSBEKREFTELSE = 1

private val log = KotlinLogging.logger {}

class DelbestillingService(
    private val delbestillingRepository: DelbestillingRepository,
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
            dellinje.copy(lagerstatusPåBestillingstidspunkt = lagerstatus)
        }
        val delbestilling = request.delbestilling.copy(deler = berikedeDellinjer)

        val delbestillingSak = delbestillingRepository.withTransaction(returnGeneratedKeys = true) { tx ->
            val saksnummer = delbestillingRepository.lagreDelbestilling(
                tx,
                bestillerFnr,
                brukersFnr,
                brukerKommunenr,
                delbestilling,
                brukersKommunenavn,
                innsendersRepresenterteOrganisasjon,
                bestillerType,
            )

            // Hent ut den nye delbestillingsaken
            val nyDelbestillingSak = delbestillingRepository.hentDelbestilling(tx, saksnummer)
                ?: throw RuntimeException("Klarte ikke hente ut delbestillingsak for saksnummer $saksnummer")

            anmodningService.lagreDelerUtenDekning(nyDelbestillingSak, tx)

            oebs.sendDelbestilling(nyDelbestillingSak, Fødselsnummer(brukersFnr), bestillersNavn)

            nyDelbestillingSak
        }

        log.info { "Delbestilling '$id' sendt inn med saksnummer '${delbestillingSak.saksnummer}'" }

        sendStatistikk(request.delbestilling, brukersFnr)

        if (!isLocal()) {
            slack.varsleOmInnsending(brukerKommunenr, brukersKommunenavn, delbestillingSak)
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

    suspend fun oppdaterStatus(saksnummer: Long, status: Status, oebsOrdrenummer: String) {
        val delbestilling = delbestillingRepository.withTransaction { tx ->
            val lagretDelbestilling = delbestillingRepository.hentDelbestilling(tx, saksnummer) ?: if (isDev()) {
                log.info { "Delbestilling $saksnummer finnes ikke i dev. Antar ugyldig testdata fra OeBS og skipper statusoppdatering." }
                return@withTransaction null
            } else {
                error("Kunne ikke oppdatere status for delbestilling $saksnummer fordi den ikke finnes.")
            }

            if (lagretDelbestilling.oebsOrdrenummer == null) {
                delbestillingRepository.oppdaterOebsOrdrenummer(tx, saksnummer, oebsOrdrenummer)
            } else if (lagretDelbestilling.oebsOrdrenummer != oebsOrdrenummer) {
                error("Mismatch i oebsOrdrenummer for delbestilling $saksnummer. Eksisterende oebsOrdrenummer: ${lagretDelbestilling.oebsOrdrenummer}. Mottatt oebsOrdrenummer: $oebsOrdrenummer")
            }

            if (lagretDelbestilling.status.ordinal < status.ordinal) {
                delbestillingRepository.oppdaterStatus(tx, saksnummer, status)
            }

            return@withTransaction lagretDelbestilling
        }

        try {
            if (delbestilling != null && status == Status.KLARGJORT) {
                val lagerstatus = oebs.hentLagerstatusForKommunenummer(
                    delbestilling.brukersKommunenummer,
                    delbestilling.delbestilling.deler.map { it.del.hmsnr })
                val lagerstatusVedInnsending =
                    delbestilling.delbestilling.deler.map { it.lagerstatusPåBestillingstidspunkt }
                log.info { "Lagerstatus for sak ${delbestilling.saksnummer} ved status=$status: $lagerstatus. Lagerstatus ved innsending: $lagerstatusVedInnsending" }
            }
        } catch (t: Throwable) {
            log.info(t) { "Forsøk på logging av lagerstatus ved status $status feilet. Ignorerer." }
        }
    }

    suspend fun oppdaterDellinjeStatus(
        oebsOrdrenummer: String,
        status: DellinjeStatus,
        hmsnr: Hmsnr,
        datoOppdatert: LocalDate,
    ) {
        require(status == DellinjeStatus.SKIPNINGSBEKREFTET) { "Forventet status ${Status.SKIPNINGSBEKREFTET} for dellinje, men fikk status $status" }

        delbestillingRepository.withTransaction { tx ->
            val lagretDelbestilling = delbestillingRepository.hentDelbestilling(tx, oebsOrdrenummer)

            if (lagretDelbestilling == null) {
                log.debug { "Ignorerer oebsOrdrenummer $oebsOrdrenummer. Fant ikke tilhørende delbestilling, antar at det ikke tilhører en delbestilling." }
                return@withTransaction
            }

            if (lagretDelbestilling.status.ordinal >= Status.SKIPNINGSBEKREFTET.ordinal) {
                log.warn { "Forsøkte å sette dellinje på $oebsOrdrenummer til SKIPNINGSBEKREFTET, men ordren har status ${lagretDelbestilling.status}" }
                return@withTransaction
            }

            val saksnummer = lagretDelbestilling.saksnummer

            // Oppdater status på dellinje
            val deler = lagretDelbestilling.delbestilling.deler.map { delLinje ->
                if (delLinje.del.hmsnr == hmsnr) {
                    metrics.delSkipningsbekreftet(lagretDelbestilling, delLinje, datoOppdatert)
                    val forventetLeveringsdato = NorwegianDateUtil.addWorkingDaysToDate(
                        datoOppdatert.toDate(),
                        LEVERINGSDAGER_FRA_SKIPNINGSBEKREFTELSE
                    )
                    delLinje.copy(
                        status = status,
                        datoSkipningsbekreftet = datoOppdatert,
                        forventetLeveringsdato = forventetLeveringsdato.toLocalDate(),
                    )

                } else {
                    delLinje
                }
            }
            val oppdatertDelbestilling = lagretDelbestilling.delbestilling.copy(deler = deler)
            delbestillingRepository.oppdaterDelbestilling(tx, saksnummer, oppdatertDelbestilling)

            // Oppdater status på ordre
            if (deler.all { it.status == DellinjeStatus.SKIPNINGSBEKREFTET }) {
                delbestillingRepository.oppdaterStatus(tx, saksnummer, Status.SKIPNINGSBEKREFTET)
            } else {
                delbestillingRepository.oppdaterStatus(tx, saksnummer, Status.DELVIS_SKIPNINGSBEKREFTET)
            }
            log.info { "Dellinje $hmsnr på sak ${lagretDelbestilling.saksnummer} (oebsnr $oebsOrdrenummer) oppdatert med status $status" }
        }
    }

    private fun validerDelbestillingRate(bestillerFnr: String, hmsnr: String, serienr: String): DelbestillingFeil? {
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
    
    fun hentDelbestillinger(bestillerFnr: String): List<DelbestillingSak> {
        return delbestillingRepository.hentDelbestillinger(bestillerFnr)
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

    fun antallDagerSidenSisteBatteribestilling(hmsnr: String, serienr: String): Long? {
        val dellbestillinger = delbestillingRepository.hentDelbestillinger(hmsnr, serienr)

        val sisteBatteribestilling = dellbestillinger.filter { bestilling ->
            bestilling.delbestilling.deler.any { dellinje ->
                dellinje.del.kategori == "Batteri"
            }
        }.maxByOrNull { it.opprettet } ?: return null

        val antallDagerSiden = sisteBatteribestilling.opprettet.toLocalDate()
            .until(LocalDate.now(), ChronoUnit.DAYS)

        return antallDagerSiden
    }

    suspend fun rapporterDelerTilAnmodning(): List<Anmodningrapport> {
        return try {
            val rapporter = anmodningService.genererAnmodningsrapporter()

            rapporter.forEach { rapport ->
                if (rapport.anmodningsbehov.isNotEmpty()) {
                    val melding = anmodningService.sendAnmodningRapport(rapport)
                    slack.varsleOmAnmodningrapportSomErSendtTilEnhet(rapport.enhetnr, melding)
                } else {
                    log.info { "Anmodningsbehov for enhetnr ${rapport.enhetnr} er tomt, alle deler har dermed fått dekning etter innsending. Hopper over." }
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

private fun LocalDate.toDate() = Date.from(this.atStartOfDay(ZoneId.systemDefault()).toInstant())
private fun Date.toLocalDate() = this.toInstant()
    .atZone(ZoneId.systemDefault())
    .toLocalDate()
