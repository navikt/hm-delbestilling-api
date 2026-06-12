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
            when (val result = oppslagService.slåOppHjelpemiddel(request.hmsnr, request.serienr)) {
                is OppslagResult.Suksess -> call.respond(result.resultat)
                is OppslagResult.Feil -> {
                    log.info { "Oppslag feilet: ${result.feil}" }
                    call.respond(HttpStatusCode.NotFound, OppslagFeilResponse(result.feil))
                }
            }
        }

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
}

