package no.nav.hjelpemidler.delbestilling.delbestilling

import io.ktor.http.HttpStatusCode
import mu.KotlinLogging
import no.nav.hjelpemidler.database.transaction
import no.nav.hjelpemidler.delbestilling.exceptions.PersonNotAccessibleInPdl
import no.nav.hjelpemidler.delbestilling.exceptions.PersonNotFoundInPdl
import no.nav.hjelpemidler.delbestilling.exceptions.TilgangException
import no.nav.hjelpemidler.delbestilling.isProd
import no.nav.hjelpemidler.delbestilling.oebs.Artikkel
import no.nav.hjelpemidler.delbestilling.oebs.OebsService
import no.nav.hjelpemidler.delbestilling.oebs.OpprettBestillingsordreRequest
import no.nav.hjelpemidler.delbestilling.pdl.PdlService
import no.nav.hjelpemidler.delbestilling.roller.DelbestillerResponse
import javax.sql.DataSource

private val log = KotlinLogging.logger {}

class DelbestillingService(
    private val dataSource: DataSource, // TODO Service burde ikke ha et forhold til datasource
    private val delbestillingRepository: DelbestillingRepository,
    private val pdlService: PdlService,
    private val oebsService: OebsService,
) {

    suspend fun opprettDelbestilling(
        delbestillerRolle: DelbestillerResponse,
        request: DelbestillingRequest,
        bestillerFnr: String
    ): DelbestillingResultat {

        validerDelbestiller(delbestillerRolle)

        val id = request.delbestilling.id
        val hmsnr = request.delbestilling.hmsnr.value
        val serienr = request.delbestilling.serienr.value
        val levering = request.delbestilling.levering

        val utlån = oebsService.hentUtlånPåArtnrOgSerienr(hmsnr, serienr)
            ?: return DelbestillingResultat(id, feil = DelbestillingFeil.INGET_UTLÅN, HttpStatusCode.NotFound)

        // val brukerFnr = "03441558383" // Test av adressebeskyttelse
        // val brukerFnr = "11111111111" // Test av person ikke funnet
        val brukerFnr = utlån.fnr

        // TODO: det føles litt feil å gjøre alle disse sjekkene her
        val brukerKommunenr = try {
            pdlService.hentKommunenummer(brukerFnr)
        } catch (e: PersonNotAccessibleInPdl) {
            log.error(e) { "Person ikke tilgjengelig i PDL" }
            return DelbestillingResultat(id, feil = DelbestillingFeil.BRUKER_IKKE_FUNNET, HttpStatusCode.NotFound)
        } catch (e: PersonNotFoundInPdl) {
            log.error(e) { "Person ikke funnet i PDL" }
            return DelbestillingResultat(id, feil = DelbestillingFeil.BRUKER_IKKE_FUNNET,  HttpStatusCode.NotFound)
        } catch (e: Exception) {
            log.error(e) { "Klarte ikke å hente bruker fra PDL" }
            throw e
        }

        // Det skal ikke være mulig å bestille til seg selv (disabler i dev pga testdata)
        if (isProd() && bestillerFnr == brukerFnr) {
            log.info { "Bestiller prøver å bestille til seg selv" }
            return DelbestillingResultat(id, feil = DelbestillingFeil.BESTILLE_TIL_SEG_SELV, HttpStatusCode.Forbidden)
        }

        // Sjekk om en av innsenders kommuner tilhører brukers kommuner
        val innsenderRepresentererBrukersKommune =
            delbestillerRolle.kommunaleOrgs?.find { it.kommunenummer == brukerKommunenr } != null

        // Skrur av denne sjekken for dev akkurat nå, da det er litt mismatch i testdataen der
        if (isProd() && !innsenderRepresentererBrukersKommune) {
            return DelbestillingResultat(id, feil = DelbestillingFeil.ULIK_GEOGRAFISK_TILKNYTNING,  HttpStatusCode.Forbidden)
        }

        transaction(dataSource) {tx ->
            delbestillingRepository.lagreDelbestilling(tx,
                bestillerFnr,
                brukerFnr,
                brukerKommunenr,
                request.delbestilling
            )
            val bestillersNavn = pdlService.hentPersonNavn(bestillerFnr, validerAdressebeskyttelse = false)
            val deler = request.delbestilling.deler.map { Artikkel(it.hmsnr, it.antall) }
            val forsendelsesinfo = if (levering == Levering.TIL_XK_LAGER) "Sendes til XK-Lager" else ""
            oebsService.sendDelbestilling(
                OpprettBestillingsordreRequest(
                    brukersFnr = brukerFnr,
                    saksnummer = id.toString(),
                    innsendernavn = bestillersNavn,
                    artikler = deler,
                    forsendelsesinfo = forsendelsesinfo,
                )
            )
        }

        log.info { "Delbestilling '$id' sendt inn" }

        return DelbestillingResultat(id, null, HttpStatusCode.Created)
    }

    private fun validerDelbestiller(delbestillerRolle: DelbestillerResponse) {
        if (!delbestillerRolle.kanBestilleDeler) {
            throw TilgangException("Innlogget bruker mangler tilgang til å bestille deler")
        }
    }

    suspend fun slåOppHjelpemiddel(hmsnr: String, serienr: String): OppslagResultat {
        val hjelpemiddel = hjelpemiddelDeler[hmsnr]
            ?: return OppslagResultat(null, OppslagFeil.TILBYR_IKKE_HJELPEMIDDEL, HttpStatusCode.NotFound)

        oebsService.hentUtlånPåArtnrOgSerienr(hmsnr, serienr)
            ?: return OppslagResultat(null, OppslagFeil.INGET_UTLÅN, HttpStatusCode.NotFound)

        return OppslagResultat(hjelpemiddel, null, HttpStatusCode.Created)
    }

    fun hentDelbestillinger(bestillerFnr: String): List<Delbestilling> {
        return delbestillingRepository.hentDelbestillinger(bestillerFnr)
    }

}
