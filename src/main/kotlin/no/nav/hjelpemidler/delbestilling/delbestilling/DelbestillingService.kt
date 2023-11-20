package no.nav.hjelpemidler.delbestilling.delbestilling

import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import no.bekk.bekkopen.date.NorwegianDateUtil
import no.nav.hjelpemidler.delbestilling.exceptions.PersonNotAccessibleInPdl
import no.nav.hjelpemidler.delbestilling.exceptions.PersonNotFoundInPdl
import no.nav.hjelpemidler.delbestilling.hjelpemidler.HjelpemiddelDeler
import no.nav.hjelpemidler.delbestilling.isDev
import no.nav.hjelpemidler.delbestilling.isProd
import no.nav.hjelpemidler.delbestilling.metrics.Metrics
import no.nav.hjelpemidler.delbestilling.oebs.Artikkel
import no.nav.hjelpemidler.delbestilling.oebs.OebsService
import no.nav.hjelpemidler.delbestilling.oebs.OpprettBestillingsordreRequest
import no.nav.hjelpemidler.delbestilling.oppslag.OppslagService
import no.nav.hjelpemidler.delbestilling.pdl.PdlService
import no.nav.hjelpemidler.delbestilling.roller.RolleService
import java.lang.RuntimeException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

private val log = KotlinLogging.logger {}

private const val LEVERINGSDAGER_FRA_SKIPNINGSBEKREFTELSE = 1

class DelbestillingService(
    private val delbestillingRepository: DelbestillingRepository,
    private val pdlService: PdlService,
    private val oebsService: OebsService,
    private val rolleService: RolleService,
    private val oppslagService: OppslagService,
    private val metrics: Metrics,
) {

    suspend fun opprettDelbestilling(
        request: DelbestillingRequest,
        bestillerFnr: String,
        tokenString: String,
    ): DelbestillingResultat {
        val id = request.delbestilling.id
        val hmsnr = request.delbestilling.hmsnr
        val serienr = request.delbestilling.serienr
        val rolle = request.delbestilling.rolle
        log.info { "Oppretter delbestilling for hmsnr $hmsnr, serienr $serienr" }

        val delbestillerRolle = rolleService.hentDelbestillerRolle(tokenString)
        log.info { "Delbestillerrolle: $delbestillerRolle " }

        val feil = validerDelbestillingRate(bestillerFnr, hmsnr, serienr)
        if (feil != null) {
            return DelbestillingResultat(id, feil = feil)
        }

        val levering = request.delbestilling.levering
        val deler = request.delbestilling.deler

        val utlån = oebsService.hentUtlånPåArtnrOgSerienr(hmsnr, serienr)
            ?: return DelbestillingResultat(id, feil = DelbestillingFeil.INGET_UTLÅN)

        val brukersFnr = utlån.fnr

        val brukerKommunenr = try {
            pdlService.hentKommunenummer(brukersFnr)
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

        // Hvis innsender med brukerpass prøver å bestille til noen andre enn seg selv, sjekk relasjonen via PDL
        if (rolle == Rolle.BRUKERPASS && bestillerFnr != brukersFnr) {
            if (!pdlService.harForeldreansvarForPerson(bestillerFnr, brukersFnr)) {
                log.info { "Bestiller med brukerpass forsøker å bestille til bruker hen ikke har foreldreansvar for" }
                return DelbestillingResultat(id, feil = DelbestillingFeil.HAR_IKKE_FORELDREANSVAR)
            }
        }

        // Det skal ikke være mulig for andre enn brukerpassbrukere å bestille til seg selv (disabler i dev pga testdata)
        if (isProd() && bestillerFnr == brukersFnr && !delbestillerRolle.erBrukerpassbruker) {
            log.info { "Bestiller med delbestillerrolle $delbestillerRolle prøver å bestille til seg selv, returnerer feilmelding" }
        }

        val brukersKommunenavn = try {
            oppslagService.hentKommune(brukerKommunenr).kommunenavn
        } catch (e: Exception) {
            // Svelg feil, kommunenavn brukes bare til statistikk så ikke krise hvis den feiler
            "Ukjent"
        }

        // Sjekk at PDL og OEBS kommunenr på bruker stemmer overens
        val oebsBrukerinfo = oebsService.hentPersoninfo(brukersFnr)
        val brukerHarSammeKommunenrIOebsOgPdl = oebsBrukerinfo.any { it.leveringKommune == brukerKommunenr }
        if (!brukerHarSammeKommunenrIOebsOgPdl) {
            log.info { "Ulik leveringsadresse. OEBS: $oebsBrukerinfo, PDL: $brukerKommunenr" }
            return DelbestillingResultat(id, feil = DelbestillingFeil.ULIK_ADRESSE_PDL_OEBS)
        }

        // Sjekk om en av innsenders kommuner tilhører brukers kommuner
        if (rolle == Rolle.TEKNIKER) {
            val innsenderRepresentererBrukersKommune =
                delbestillerRolle.kommunaleOrgs?.find { it.kommunenummer == brukerKommunenr } != null

            // Skrur av denne sjekken for dev akkurat nå, da det er litt mismatch i testdataen der
            if (isProd() && !innsenderRepresentererBrukersKommune) {
                return DelbestillingResultat(
                    id,
                    feil = DelbestillingFeil.ULIK_GEOGRAFISK_TILKNYTNING,
                )
            }
        }

        val bestillersNavn = pdlService.hentPersonNavn(bestillerFnr, validerAdressebeskyttelse = false)
        val artikler = deler.map { Artikkel(it.del.hmsnr, it.antall) }
        val xkLagerInfo = if (levering == Levering.TIL_XK_LAGER) "XK-Lager " else ""
        val forsendelsesinfo = when(rolle) {
            Rolle.TEKNIKER -> "${xkLagerInfo}Tekniker: $bestillersNavn"
            Rolle.BRUKERPASS -> "Brukerpassbruker: $bestillersNavn"
        }

        val delbestillingSak = delbestillingRepository.withTransaction(returnGeneratedKeys = true) { tx ->
            val saksnummer = delbestillingRepository.lagreDelbestilling(
                tx,
                bestillerFnr,
                brukersFnr,
                brukerKommunenr,
                request.delbestilling,
                brukersKommunenavn,
            )

            // Hent ut den nye delbestillingsaken
            val nyDelbestillingSak = if (saksnummer != null) {
                delbestillingRepository.hentDelbestilling(tx, saksnummer)
            } else {
                null
            }

            if (nyDelbestillingSak == null) {
                throw RuntimeException("Klarte ikke hente ut delbestillingsak for saksnummer $saksnummer")
            }

            oebsService.sendDelbestilling(
                OpprettBestillingsordreRequest(
                    brukersFnr = brukersFnr,
                    saksnummer = saksnummer.toString(),
                    innsendernavn = bestillersNavn,
                    artikler = artikler,
                    forsendelsesinfo = forsendelsesinfo,
                )
            )

            nyDelbestillingSak
        }

        log.info { "Delbestilling '$id' sendt inn med saksnummer '${delbestillingSak.saksnummer}'" }

        sendStatistikk(request.delbestilling, utlån.fnr)

        return DelbestillingResultat(id, null, delbestillingSak.saksnummer, delbestillingSak)
    }

    suspend fun sendStatistikk(delbestilling: Delbestilling, fnrBruker: String) = coroutineScope {
        launch {
            try {
                val navnHovedprodukt = HjelpemiddelDeler.hentHjelpemiddelMedDeler(delbestilling.hmsnr)?.navn ?: "Ukjent"
                val hjmbrukerHarBrukerpass = oebsService.harBrukerpass(fnrBruker)
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
        delbestillingRepository.withTransaction { tx ->
            val lagretDelbestilling = delbestillingRepository.hentDelbestilling(tx, saksnummer)!!

            if (lagretDelbestilling.oebsOrdrenummer == null) {
                delbestillingRepository.oppdaterOebsOrdrenummer(tx, saksnummer, oebsOrdrenummer)
            } else if (lagretDelbestilling.oebsOrdrenummer != oebsOrdrenummer) {
                error("Mismatch i oebsOrdrenummer for delbestilling $saksnummer. Eksisterende oebsOrdrenummer: ${lagretDelbestilling.oebsOrdrenummer}. Mottatt oebsOrdrenummer: $oebsOrdrenummer")
            }

            if (lagretDelbestilling.status.ordinal < status.ordinal) {
                delbestillingRepository.oppdaterStatus(tx, saksnummer, status)
            }
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

    suspend fun slåOppHjelpemiddel(hmsnr: String, serienr: String): OppslagResultat {
        val hjelpemiddelMedDeler = HjelpemiddelDeler.hentHjelpemiddelMedDeler(hmsnr)
            ?: return OppslagResultat(null, OppslagFeil.TILBYR_IKKE_HJELPEMIDDEL, HttpStatusCode.NotFound)

        oebsService.hentUtlånPåArtnrOgSerienr(hmsnr, serienr)
            ?: return OppslagResultat(null, OppslagFeil.INGET_UTLÅN, HttpStatusCode.NotFound)

        return OppslagResultat(hjelpemiddelMedDeler, null, HttpStatusCode.OK)
    }

    fun hentDelbestillinger(bestillerFnr: String): List<DelbestillingSak> {
        return delbestillingRepository.hentDelbestillinger(bestillerFnr)
    }
}

private fun LocalDate.toDate() = Date.from(this.atStartOfDay(ZoneId.systemDefault()).toInstant())
private fun Date.toLocalDate() = this.toInstant()
    .atZone(ZoneId.systemDefault())
    .toLocalDate()
