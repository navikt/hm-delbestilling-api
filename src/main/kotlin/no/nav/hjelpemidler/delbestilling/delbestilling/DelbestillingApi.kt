package no.nav.hjelpemidler.delbestilling.delbestilling

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tms.token.support.tokenx.validation.user.TokenXUserFactory

private val log = KotlinLogging.logger {}

fun Route.delbestillingApi(
) {
    post("/oppslag") {
        log.info { "kall til /oppslag" }
        val request = call.receive<OppslagRequest>()
        log.info { "request: $request" }
        val hjelpemiddel = hjelpemiddelDeler[request.artNr]
        val serieNrKobletMotBuker = request.serieNr != "000000"

        call.respond(OppslagResponse(hjelpemiddel, serieNrKobletMotBuker))
    }
}

fun Route.delbestillingApiAuthenticated(
    delbestillingRepository: DelbestillingRepository,
    tokenXUserFactory: TokenXUserFactory = TokenXUserFactory,
) {

    post("/delbestilling") {

        // TODO endepunktet må ligge bak autentisering
        val request = call.receive<Delbestilling>()
        log.info { "/delbestilling request: $request" }
        val bestillerFnr = tokenXUserFactory.createTokenXUser(call).ident

        val brukerFnr = "12345678910" // TODO hent fra OEBS via artnr+serienr
        val brukerKommunenr = "0301" // Oslo TODO hent fra PDL

        // TODO transaction {
        delbestillingRepository.lagreDelbestilling(bestillerFnr, brukerFnr, brukerKommunenr, request)
        // send til OEBS
        // }

        log.info { "Delbestilling '${request.id}' sendt inn" }

        call.respond(status = HttpStatusCode.Created, request.id)

    }

    get("/delbestilling") {
        val bestillerFnr = tokenXUserFactory.createTokenXUser(call).ident
        val delbestillinger = delbestillingRepository.hentDelbestillinger(bestillerFnr)
        call.respond(delbestillinger)
    }
}
