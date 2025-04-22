package no.nav.hjelpemidler.delbestilling.hjelpemidler

import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.hjelpemiddelApi(
    hjelpemidlerService: HjelpemidlerService
) {
    get("/hjelpemiddel-titler") {
        call.respond(
            HjelpemiddelTitlerResponse(hjelpemidlerService.hentAlleHjelpemiddelTitlerCached())
        )
    }
}

private data class HjelpemiddelTitlerResponse(
    val titler: Set<String>
)