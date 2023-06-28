package no.nav.hjelpemidler.hjelpemidler.hjelpemidler

import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import no.nav.hjelpemidler.delbestilling.hjelpemidler.HjelpemidlerService

fun Route.hjelpemiddelApi(
    hjelpemidlerService: HjelpemidlerService
) {
    get("/hjelpemidler") {
        val hjelpemidlerMedDeler = hjelpemidlerService.hentAlleHjelpemidlerMedDeler()
        call.respond(hjelpemidlerMedDeler)
    }
}
