package no.nav.hjelpemidler.delbestilling.delbestilling

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.hjelpemidler.delbestilling.oebs.OebsProxyApiService
import no.nav.hjelpemidler.delbestilling.pdl.PdlClient
import no.nav.hjelpemidler.delbestilling.roller.RolleService
import no.nav.tms.token.support.tokenx.validation.user.TokenXUserFactory

private val log = KotlinLogging.logger {}

fun Route.delbestillingApi(
    oebsProxyApiService: OebsProxyApiService
) {
    post("/oppslag") {
        try {
            val request = call.receive<OppslagRequest>()
            log.info { "/oppslag request: $request" }

            val utlån = oebsProxyApiService.hentUtlånPåArtnrOgSerienr(request.artnr, request.serienr)
            log.info { "utlån: $utlån" }

            val hjelpemiddel = hjelpemiddelDeler[request.artnr]
            val serienrKobletMotBruker = request.serienr != "000000"

            call.respond(OppslagResponse(hjelpemiddel, serienrKobletMotBruker))
        } catch(e: Exception) {
            log.error(e) {"Klarte ikke gjøre oppslag"}
        }
    }
}

fun Route.delbestillingApiAuthenticated(
    delbestillingRepository: DelbestillingRepository,
    rolleService: RolleService,
    pdlClient: PdlClient,
    oebsProxyApiService: OebsProxyApiService,
    tokenXUserFactory: TokenXUserFactory = TokenXUserFactory,
) {

    post("/delbestilling") {
        try {

            val request = call.receive<Delbestilling>()
            log.info { "/delbestilling request: $request" }
            val tokenXUser = tokenXUserFactory.createTokenXUser(call)
            val bestillerFnr = tokenXUser.ident

            val kanBestilleDeler = rolleService.harDelbestillerRolle(tokenXUser.tokenString)
            if (kanBestilleDeler == false) {
                call.respond(HttpStatusCode.Forbidden, "Du har ikke rettighet til å gjøre dette")
            }

            val utlån = oebsProxyApiService.hentUtlånPåArtnrOgSerienr(request.hmsnr.toString(), request.serienr.toString())
            log.info { "utlån: $utlån" }
            // TODO: kanskje ikke 404 er den beste responsen her
            val brukerFnr = utlån?.fnr ?: return@post call.respond(HttpStatusCode.NotFound, "Det er ingen bruker knyttet til dette utlånet")

            val brukerKommunenr = pdlClient.hentKommunenummer(brukerFnr)
            log.info { brukerKommunenr }

            // TODO transaction {
            delbestillingRepository.lagreDelbestilling(bestillerFnr, brukerFnr, brukerKommunenr, request)
            // send til OEBS
            // }

            log.info { "Delbestilling '${request.id}' sendt inn" }

            call.respond(status = HttpStatusCode.Created, request.id)
        } catch (e: Exception) {
            log.error { "noe feila: $e ${e.stackTraceToString()}" }
            throw e
        }

    }

    get("/delbestilling") {
        val bestillerFnr = tokenXUserFactory.createTokenXUser(call).ident
        val delbestillinger = delbestillingRepository.hentDelbestillinger(bestillerFnr)
        call.respond(delbestillinger)
    }
}
