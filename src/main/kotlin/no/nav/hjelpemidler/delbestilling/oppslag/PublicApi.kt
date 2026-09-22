package no.nav.hjelpemidler.delbestilling.oppslag

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import no.nav.hjelpemidler.delbestilling.delbestilling.requireHmsnr


private val log = KotlinLogging.logger {}

fun Route.publicApi(
    oppslagService: OppslagService,
) {
    get("/hjelpemidler/{hmsnr}") {
        val hmsnr = requireHmsnr(call.parameters["hmsnr"])
        log.info { "GET /hjelpemidler/$hmsnr" }
        when (val result = oppslagService.slåOppHjelpemiddel(hmsnr)) {
            is OppslagResultUtenDeler.Suksess -> call.respond(result.resultat)
            is OppslagResultUtenDeler.Feil -> {
                log.info { "Oppslag feilet: ${result.feil}" }
                call.respond(HttpStatusCode.NotFound, OppslagFeilResponse(result.feil))
            }
        }
    }
}



