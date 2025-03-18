package no.nav.hjelpemidler.delbestilling.hjelpemidler

import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.delliste


fun Route.hjelpemiddelApi(
    hjelpemidlerService: HjelpemidlerService
) {
    get("/hjelpemidler") {
        val hjelpemidlerMedDeler = hjelpemidlerService.hentAlleHjelpemidlerMedDeler()
        call.respond(hjelpemidlerMedDeler)
    }

    get("/hjelpemiddel-titler") {
        call.respond(
            HjelpemiddelTitlerResponse(hjelpemidlerService.hentAlleHjelpemiddelTitler())
        )
    }


    get("/deler") {
        call.respond(delliste)
    }

}

private data class HjelpemiddelTitlerResponse(
    val titler: Set<String>
)