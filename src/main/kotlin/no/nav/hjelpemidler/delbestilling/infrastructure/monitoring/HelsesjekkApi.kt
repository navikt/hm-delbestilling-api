package no.nav.hjelpemidler.delbestilling.infrastructure.monitoring

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.helsesjekkApi() {
    get("/isalive") {
        call.respondText("ALIVE", status = HttpStatusCode.OK)
    }

    get("/isready") {
        call.respondText("READY", status = HttpStatusCode.OK)
    }
}
