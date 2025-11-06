package no.nav.hjelpemidler.delbestilling.rapportering

import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post


fun Route.rapporteringRoutes(
    rapporteringService: RapporteringService,
) {
    post("/rapportering/start") {
        call.respond(rapporteringService.startRapporteringsjobber())
    }
}
