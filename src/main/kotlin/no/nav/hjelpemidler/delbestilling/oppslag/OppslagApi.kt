package no.nav.hjelpemidler.delbestilling.oppslag

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

private val log = KotlinLogging.logger {}

fun Route.oppslagApi(
    oppslagService: OppslagService,
) {
    post("/oppslag-brukernr") {
        val request = call.receive<OppslagBrukernrRequest>()
        log.info { "/oppslag-brukernr request: $request" }
        when (val result = oppslagService.slåOppHjelpemiddelMedBrukernr(request.hmsnr, request.brukernr)) {
            is OppslagResult.Suksess -> call.respond(result.resultat)
            is OppslagResult.Feil -> {
                log.info { "Oppslag med brukernr. feilet: ${result.feil}" }
                call.respond(HttpStatusCode.NotFound, OppslagFeilResponse(result.feil))
            }
        }
    }
}