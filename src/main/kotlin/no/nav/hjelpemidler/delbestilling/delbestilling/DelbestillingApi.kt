package no.nav.hjelpemidler.delbestilling.delbestilling

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.hjelpemidler.delbestilling.isProd
import no.nav.hjelpemidler.delbestilling.oebs.Artikkel
import no.nav.hjelpemidler.delbestilling.oebs.OebsService
import no.nav.hjelpemidler.delbestilling.oebs.OpprettBestillingsordreRequest
import no.nav.hjelpemidler.delbestilling.pdl.PdlClient
import no.nav.hjelpemidler.delbestilling.roller.RolleService
import no.nav.tms.token.support.tokenx.validation.user.TokenXUserFactory

private val log = KotlinLogging.logger {}

fun Route.delbestillingApi(
    oebsService: OebsService
) {
    post("/oppslag") {
        try {
            val request = call.receive<OppslagRequest>()
            log.info { "/oppslag request: $request" }

            val hjelpemiddel = hjelpemiddelDeler[request.artnr]
                ?: return@post call.respond(OppslagResponse(null, OppslagFeil.TILBYR_IKKE_HJELPEMIDDEL))

            oebsService.hentUtlånPåArtnrOgSerienr(request.artnr, request.serienr)
                ?: return@post call.respond(OppslagResponse(null, OppslagFeil.INGET_UTLÅN))

            call.respond(OppslagResponse(hjelpemiddel, null))
        } catch (e: Exception) {
            log.error(e) { "Klarte ikke gjøre oppslag" }
        }
    }
}

fun Route.delbestillingApiAuthenticated(
    delbestillingRepository: DelbestillingRepository,
    rolleService: RolleService,
    pdlClient: PdlClient,
    oebsService: OebsService,
    tokenXUserFactory: TokenXUserFactory = TokenXUserFactory,
) {

    post("/delbestilling") {
        try {
            val request = call.receive<Delbestilling>()
            log.info { "/delbestilling request: $request" }
            val tokenXUser = tokenXUserFactory.createTokenXUser(call)
            val bestillerFnr = tokenXUser.ident

            val delbestillerRolle = rolleService.hentDelbestillerRolle(tokenXUser.tokenString)
            log.info { "delbestillerRolle: $delbestillerRolle" }
            if (!delbestillerRolle.kanBestilleDeler) {
                call.respond(HttpStatusCode.Forbidden, "Du har ikke rettighet til å gjøre dette")
            }

            val utlån = oebsService.hentUtlånPåArtnrOgSerienr(request.hmsnr.value, request.serienr.value)
            val brukerFnr = utlån?.fnr ?: return@post call.respond(DelbestillingResponse(request.id, feil = DelbestillingFeil.INGET_UTLÅN))

            val brukerKommunenr = pdlClient.hentKommunenummer(brukerFnr)

            // Sjekk om en av innsenders kommuner tilhører brukers kommuner
            val innsenderRepresentererBrukersKommune =
                delbestillerRolle.kommunaleOrgs?.find { it.kommunenummer == brukerKommunenr } != null

            // Skrur av denne sjekken for dev akkurat nå, da det er litt mismatch i testdataen der
            if (isProd() && !innsenderRepresentererBrukersKommune) {
                call.respond(DelbestillingResponse(request.id, feil = DelbestillingFeil.ULIK_GEOGRAFISK_TILKNYTNING))
            }

            // TODO transaction {
            delbestillingRepository.lagreDelbestilling(bestillerFnr, brukerFnr, brukerKommunenr, request)
            val saksnummer =
                request.id // TODO hva med å bruker et saksnummer alá "del-123"? For å skille frå behovsmelding saksnr.
            val bestillersNavn = "Tekniker Reservedelsen" // TODO hent frå PDL (har tilsvarande spørring i hm-soknad-api
            val deler = request.deler.map { Artikkel(it.hmsnr, it.antall) }
            oebsService.sendDelbestilling(
                OpprettBestillingsordreRequest(
                    brukerFnr = brukerFnr,
                    saksnummer = saksnummer.toString(),
                    bestillersNavn = bestillersNavn,
                    deler = deler
                )
            )
            // }

            log.info { "Delbestilling '${request.id}' sendt inn" }

            call.respond(status = HttpStatusCode.Created, request.id)
        } catch (e: Exception) {
            log.error(e) { "noe feila" }
            throw e
        }
    }

    get("/delbestilling") {
        val bestillerFnr = tokenXUserFactory.createTokenXUser(call).ident
        val delbestillinger = delbestillingRepository.hentDelbestillinger(bestillerFnr)
        call.respond(delbestillinger)
    }
}
