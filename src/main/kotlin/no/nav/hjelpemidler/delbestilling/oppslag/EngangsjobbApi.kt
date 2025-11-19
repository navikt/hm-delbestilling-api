package no.nav.hjelpemidler.delbestilling.oppslag

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import no.nav.hjelpemidler.delbestilling.delbestilling.EngangsjobbService

fun Route.engangsJobbApi(
    engangsjobbService: EngangsjobbService
) {
    post("/generer-enheter") {
        val respons = engangsjobbService.genererEnheter()
        call.respond(respons)
    }
}

