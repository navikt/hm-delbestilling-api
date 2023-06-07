package no.nav.hjelpemidler.delbestilling.exceptions

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

class PersonNotFoundInPdl(message: String) : RuntimeException(message)

class PersonNotAccessibleInPdl(message: String = "") : RuntimeException(message)

class PdlRequestFailedException(message: String = "") : RuntimeException("Request til PDL feilet $message")

class PdlResponseMissingData(message: String = "") : RuntimeException("Response from PDL mangler nødvendig data $message")

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<PersonNotFoundInPdl> { call, _ ->
            call.respond(HttpStatusCode.NotFound)
        }
        exception<PersonNotAccessibleInPdl> { call, _ ->
            call.respond(HttpStatusCode.Forbidden)
        }
        exception<PdlRequestFailedException> { call, _ ->
            call.respond(HttpStatusCode.InternalServerError)
        }
        exception<PdlResponseMissingData> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, cause.message!!)
        }
    }
}
