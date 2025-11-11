package no.nav.hjelpemidler.delbestilling.oppslag

import io.github.oshai.kotlinlogging.KotlinLogging
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
    get("/hjelpemiddel-titler") {
        call.respond(hjelpemiddeloversikt.hentAlleHjelpemiddelTitlerCached())
    }

    rateLimit(RateLimitName("rateLimitOppslag")) {
        post("/oppslag") {
            val request = call.receive<OppslagRequest>()
            log.info { "/oppslag request: $request" }
            call.respond(oppslagService.slåOppHjelpemiddel(request.hmsnr, request.serienr))
        }
    }
}

