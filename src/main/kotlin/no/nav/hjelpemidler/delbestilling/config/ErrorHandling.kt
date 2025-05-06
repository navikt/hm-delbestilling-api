package no.nav.hjelpemidler.delbestilling.config

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.MissingRequestParameterException
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.PdlRequestFailedException
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.PdlResponseMissingData
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.PersonNotAccessibleInPdl
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.PersonNotFoundInPdl

private val log = KotlinLogging.logger {}

fun Application.configureErrorHandling() {
    install(StatusPages) {
        // PDL
        exception<PersonNotFoundInPdl> { call, cause ->
            call.respond(HttpStatusCode.NotFound, cause.message.orUnknown())
        }
        exception<PersonNotAccessibleInPdl> { call, _ ->
            call.respond(HttpStatusCode.Forbidden)
        }
        exception<PdlRequestFailedException> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, cause.message.orUnknown())
        }
        exception<PdlResponseMissingData> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, cause.message.orUnknown())
        }
        exception<PdlResponseMissingData> { call, cause ->
            call.respond(HttpStatusCode.Forbidden, cause.message.orUnknown())
        }

        // General
        exception<RequestValidationException> { call, cause ->
            log.error(cause) { "BadRequest (fix validering i frontend)" }
            call.respond(HttpStatusCode.BadRequest, cause.reasons.joinToString())
        }
        exception<MissingRequestParameterException> {call, cause ->
            val message = "Mangler \"${cause.parameterName}\" parameter i request"
            log.error(cause) { message }
            call.respond(HttpStatusCode.BadRequest, message)
        }
        exception<Exception> { call, cause ->
            log.error(cause) { "Unhandled exception." }
            call.respond(HttpStatusCode.InternalServerError)
        }

    }
}

private fun (String?).orUnknown() = this ?: "Unknown error"