package no.nav.hjelpemidler.delbestilling.exceptions

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import mu.KotlinLogging

private val log = KotlinLogging.logger {}

class PersonNotFoundInPdl(message: String) : RuntimeException(message)

class PersonNotAccessibleInPdl(message: String = "") : RuntimeException(message)

class PdlRequestFailedException(message: String = "") : RuntimeException("Request til PDL feilet $message")

class PdlResponseMissingData(message: String = "") :
    RuntimeException("Response from PDL mangler nødvendig data $message")

class TilgangException(message: String) : RuntimeException("Innlogget bruker har ikke riktig tilgang. $message")

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<PersonNotFoundInPdl> { call, cause ->
            call.respond(HttpStatusCode.NotFound, cause.message!!)
        }
        exception<PersonNotAccessibleInPdl> { call, _ ->
            call.respond(HttpStatusCode.Forbidden)
        }
        exception<PdlRequestFailedException> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, cause.message!!)
        }
        exception<PdlResponseMissingData> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, cause.message!!)
        }
        exception<PdlResponseMissingData> { call, cause ->
            call.respond(HttpStatusCode.Forbidden, cause.message!!)
        }
        exception<Exception> { call, cause ->
            log.error(cause) { "Unhandled exception." }
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
}
