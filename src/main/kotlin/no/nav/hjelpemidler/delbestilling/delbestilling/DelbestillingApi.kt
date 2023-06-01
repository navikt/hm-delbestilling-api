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
        log.info { "kall til /oppslag" }
        val request = call.receive<OppslagRequest>()
        log.info { "request: $request" }

        val utlån = oebsProxyApiService.hentUtlånPåArtnrOgSerienr(request.artnr, request.serienr)
        log.info { "utlån: $utlån" }

        val hjelpemiddel = hjelpemiddelDeler[request.artnr]
        val serienrKobletMotBuker = request.serienr != "000000"

        call.respond(OppslagResponse(hjelpemiddel, serienrKobletMotBuker))
    }
}

fun Route.delbestillingApiAuthenticated(
    delbestillingRepository: DelbestillingRepository,
    rolleService: RolleService,
    tokenXUserFactory: TokenXUserFactory = TokenXUserFactory,
    pdlClient: PdlClient
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

            val brukerFnr = "26848497710" // TODO hent fra OEBS via artnr+serienr

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
