package no.nav.hjelpemidler.delbestilling.delbestilling

import io.ktor.http.HttpStatusCode
import mu.KotlinLogging
import no.nav.hjelpemidler.delbestilling.exceptions.PersonNotAccessibleInPdl
import no.nav.hjelpemidler.delbestilling.exceptions.PersonNotFoundInPdl
import no.nav.hjelpemidler.delbestilling.exceptions.TilgangException
import no.nav.hjelpemidler.delbestilling.hjelpemidler.HjelpemiddelDeler
import no.nav.hjelpemidler.delbestilling.isDev
import no.nav.hjelpemidler.delbestilling.isProd
import no.nav.hjelpemidler.delbestilling.oebs.Artikkel
import no.nav.hjelpemidler.delbestilling.oebs.OebsService
import no.nav.hjelpemidler.delbestilling.oebs.OpprettBestillingsordreRequest
import no.nav.hjelpemidler.delbestilling.pdl.PdlService
import no.nav.hjelpemidler.delbestilling.roller.Delbestiller
import java.time.LocalDateTime

private val log = KotlinLogging.logger {}

class DelbestillingService(
    private val delbestillingRepository: DelbestillingRepository,
    private val pdlService: PdlService,
    private val oebsService: OebsService,
) {

    suspend fun opprettDelbestilling(
        delbestillerRolle: Delbestiller,
        request: DelbestillingRequest,
        bestillerFnr: String,
    ): DelbestillingResultat {
        val id = request.delbestilling.id
        val hmsnr = request.delbestilling.hmsnr
        val serienr = request.delbestilling.serienr

        validerDelbestillerTilgang(delbestillerRolle)
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

        // Det skal ikke være mulig å bestille til seg selv (disabler i dev pga testdata)
        if (isProd() && bestillerFnr == brukersFnr) {
            log.info { "Bestiller prøver å bestille til seg selv" }
            return DelbestillingResultat(id, feil = DelbestillingFeil.BESTILLE_TIL_SEG_SELV)
        }

        // Sjekk at PDL og OEBS kommunenr på bruker stemmer overens
        val oebsBrukerinfo = oebsService.hentPersoninfo(brukersFnr)
        val brukerHarSammeKommunenrIOebsOgPdl = oebsBrukerinfo.any { it.leveringKommune == brukerKommunenr }
        if (!brukerHarSammeKommunenrIOebsOgPdl) {
            log.info { "Ulik leveringsadresse. OEBS: $oebsBrukerinfo, PDL: $brukerKommunenr" }
            return DelbestillingResultat(id, feil = DelbestillingFeil.ULIK_ADRESSE_PDL_OEBS)
        }

        // Sjekk om en av innsenders kommuner tilhører brukers kommuner
        val innsenderRepresentererBrukersKommune =
            delbestillerRolle.kommunaleOrgs?.find { it.kommunenummer == brukerKommunenr } != null

        // Skrur av denne sjekken for dev akkurat nå, da det er litt mismatch i testdataen der
        if (isProd() && !innsenderRepresentererBrukersKommune) {
            return DelbestillingResultat(
                id,
                feil = DelbestillingFeil.ULIK_GEOGRAFISK_TILKNYTNING,
            )
        }

        val bestillersNavn = pdlService.hentPersonNavn(bestillerFnr, validerAdressebeskyttelse = false)
        val artikler = deler.map { Artikkel(it.del.hmsnr, it.antall) }
        val xkLagerInfo = if (levering == Levering.TIL_XK_LAGER) "XK-Lager " else ""
        val forsendelsesinfo = "${xkLagerInfo}Tekniker: $bestillersNavn"

        val lagretSaksnummer = delbestillingRepository.withTransaction(returnGeneratedKeys = true) { tx ->
            val saksnummer = delbestillingRepository.lagreDelbestilling(
                tx,
                bestillerFnr,
                brukersFnr,
                brukerKommunenr,
                request.delbestilling
            )
            oebsService.sendDelbestilling(
                OpprettBestillingsordreRequest(
                    brukersFnr = brukersFnr,
                    saksnummer = saksnummer.toString(),
                    innsendernavn = bestillersNavn,
                    artikler = artikler,
                    forsendelsesinfo = forsendelsesinfo,
                )
            )
            saksnummer
        }

        log.info { "Delbestilling '$id' sendt inn med saksnummer '$lagretSaksnummer'" }

        return DelbestillingResultat(id, null, saksnummer = lagretSaksnummer)
    }

    private fun validerDelbestillingRate(bestillerFnr: String, hmsnr: String, serienr: String): DelbestillingFeil? {
        if (isDev()) {
            return null // For enklere testing i dev
        }
        val maxAntallBestillingerPer24Timer = 2
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

    private fun validerDelbestillerTilgang(delbestillerRolle: Delbestiller) {
        if (!delbestillerRolle.kanBestilleDeler) {
            throw TilgangException("Innlogget bruker mangler tilgang til å bestille deler")
        }
    }

    suspend fun slåOppHjelpemiddel(hmsnr: String, serienr: String): OppslagResultat {
        val hjelpemiddelMedDeler = HjelpemiddelDeler.hentHjelpemiddelMedDeler(hmsnr)
            ?: return OppslagResultat(null, OppslagFeil.TILBYR_IKKE_HJELPEMIDDEL, HttpStatusCode.NotFound)

        oebsService.hentUtlånPåArtnrOgSerienr(hmsnr, serienr)
            ?: return OppslagResultat(null, OppslagFeil.INGET_UTLÅN, HttpStatusCode.NotFound)

        return OppslagResultat(hjelpemiddelMedDeler, null, HttpStatusCode.OK)
    }

    fun hentDelbestillinger(bestillerFnr: String): List<LagretDelbestilling> {
        return delbestillingRepository.hentDelbestillinger(bestillerFnr)
    }
}