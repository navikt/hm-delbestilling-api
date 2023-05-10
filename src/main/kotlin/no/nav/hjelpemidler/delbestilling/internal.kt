package no.nav.hjelpemidler.delbestilling

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.apache.kafka.clients.consumer.KafkaConsumer

fun Route.internal() {
    get("/isalive") {
        call.respondText("ALIVE", status = HttpStatusCode.OK)
    }

    get("/isready") {
        call.respondText("READY", status = HttpStatusCode.OK)
    }
}
