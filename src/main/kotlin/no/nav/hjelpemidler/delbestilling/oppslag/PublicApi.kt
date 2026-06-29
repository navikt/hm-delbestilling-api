package no.nav.hjelpemidler.delbestilling.oppslag

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post


private val log = KotlinLogging.logger {}

fun Route.publicApi(
    hjelpemiddeloversikt: Hjelpemiddeloversikt,
    oppslagService: OppslagService,
) {
    get("/tilgjengelige-hjelpemidler") {
        call.respond(hjelpemiddeloversikt.hentTilgjengeligeHjelpemidlerCached())
    }

    post("/deler-til-hmsnrs") {
        val hmsnrs = call.receive<DelerTilHmsnrsRequest>().hmsnrs
        call.respond(hjelpemiddeloversikt.hentDelerTilHmsnrs(hmsnrs))
    }

    rateLimit(RateLimitName("rateLimitOppslag")) {
        post("/oppslag") {
            val request = call.receive<OppslagRequest>()
            log.info { "/oppslag request: $request" }
            when (val result = oppslagService.slåOppHjelpemiddelMedSerienr(request.hmsnr, request.serienr)) {
                is OppslagResult.Suksess -> call.respond(result.resultat)
                is OppslagResult.Feil -> {
                    log.info { "Oppslag feilet: ${result.feil}" }
                    call.respond(HttpStatusCode.NotFound, OppslagFeilResponse(result.feil))
                }
            }
        }
    }

    get("/hjelpemidler/{hmsnr}") {
        val hmsnr = call.parameters["hmsnr"] ?: throw IllegalArgumentException("Mangler hmsnr")
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



