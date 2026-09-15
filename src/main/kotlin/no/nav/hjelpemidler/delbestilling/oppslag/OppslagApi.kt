package no.nav.hjelpemidler.delbestilling.oppslag

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import no.nav.hjelpemidler.delbestilling.delbestilling.requireHmsnr
import no.nav.hjelpemidler.logging.teamInfo

private val log = KotlinLogging.logger {}

fun Route.oppslagApi(
    oppslagService: OppslagService,
) {
    post("/hjelpemidler/{hmsnr}/deler") {
        val hmsnr = requireHmsnr(call.parameters["hmsnr"])
        val request = call.receive<OppslagDelerRequest>()
        log.info { "Slår opp deler for hmsnr=$hmsnr" }
        log.teamInfo { "/hjelpemidler/$hmsnr/deler request: $request" }
        when (val result =
            oppslagService.slåOppDeler(hmsnr = hmsnr, brukernr = request.brukernr, serienr = request.serienr)) {
            is OppslagResult.Suksess -> call.respond(result.resultat)
            is OppslagResult.Feil -> {
                log.info { "Oppslag feilet: ${result.feil}" }
                call.respond(HttpStatusCode.NotFound, OppslagFeilResponse(result.feil))
            }
        }
    }
}