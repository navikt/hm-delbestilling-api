package no.nav.hjelpemidler.delbestilling.delbestilling

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.hjelpemidler.delbestilling.roller.RolleService
import no.nav.tms.token.support.tokenx.validation.user.TokenXUserFactory

private val log = KotlinLogging.logger {}

fun Route.delbestillingApi(
    delbestillingService: DelbestillingService,
) {
    post("/oppslag") {
        try {
            val request = call.receive<OppslagRequest>()
            log.info { "/oppslag request: $request" }

            val resultat = delbestillingService.slåOppHjelpemiddel(request.hmsnr, request.serienr)

            val oppslagResponse = OppslagResponse(resultat.hjelpemiddel, resultat.feil)

            call.respond(resultat.httpStatusCode, oppslagResponse)
        } catch (e: Exception) {
            log.error(e) { "Klarte ikke gjøre oppslag" }
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
}

fun Route.delbestillingApiAuthenticated(
    rolleService: RolleService,
    delbestillingService: DelbestillingService,
    tokenXUserFactory: TokenXUserFactory = TokenXUserFactory,
) {
    post("/delbestilling") {
        try {
            val request = call.receive<DelbestillingRequest>()
            log.info { "/delbestilling request: $request" }
            val tokenXUser = tokenXUserFactory.createTokenXUser(call)
            val bestillerFnr = tokenXUser.ident

            val delbestillerRolle = rolleService.hentDelbestillerRolle(tokenXUser.tokenString)
            log.info { "delbestillerRolle: $delbestillerRolle" }

            val resultat = delbestillingService.opprettDelbestilling(delbestillerRolle, request, bestillerFnr)

            val delbestillingResponse = DelbestillingResponse(resultat.id, resultat.feil)

            call.respond(resultat.httpStatusCode, delbestillingResponse)
        } catch (e: Exception) {
            log.error(e) { "Innsending av bestilling feilet" }
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

    get("/delbestilling") {
        val bestillerFnr = tokenXUserFactory.createTokenXUser(call).ident
        val delbestillinger = delbestillingService.hentDelbestillinger(bestillerFnr)
        call.respond(delbestillinger)
    }
}
