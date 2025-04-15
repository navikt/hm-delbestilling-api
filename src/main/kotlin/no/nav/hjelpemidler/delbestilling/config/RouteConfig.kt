package no.nav.hjelpemidler.delbestilling.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.authentication
import io.ktor.server.plugins.callid.CALL_ID_DEFAULT_DICTIONARY
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.plugins.callid.generate
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.requestvalidation.RequestValidation
import io.ktor.server.plugins.requestvalidation.ValidationResult
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.request.uri
import io.ktor.server.routing.IgnoreTrailingSlash
import no.nav.hjelpemidler.delbestilling.delbestilling.model.DelbestillingRequest
import no.nav.hjelpemidler.delbestilling.delbestilling.model.OppslagRequest
import no.nav.hjelpemidler.delbestilling.delbestilling.validateDelbestillingRequest
import no.nav.hjelpemidler.delbestilling.delbestilling.validateOppslagRequest
import no.nav.hjelpemidler.delbestilling.infrastructure.CORRELATION_ID_HEADER
import no.nav.hjelpemidler.delbestilling.infrastructure.CORRELATION_ID_KEY
import no.nav.hjelpemidler.delbestilling.infrastructure.monitoring.configureStatusPages
import no.nav.tms.token.support.azure.validation.azure
import no.nav.tms.token.support.tokenx.validation.mock.LevelOfAssurance
import no.nav.tms.token.support.tokenx.validation.mock.tokenXMock
import no.nav.tms.token.support.tokenx.validation.tokenX
import org.slf4j.event.Level
import java.util.TimeZone
import kotlin.time.Duration.Companion.seconds

fun Application.configure() {
    TimeZone.setDefault(TimeZone.getTimeZone("Europe/Oslo"))

    install(ContentNegotiation) {
        jackson {
            registerModule(JavaTimeModule())
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }
    }

    install(IgnoreTrailingSlash)

    install(RateLimit) {
        register(RateLimitName("public")) {
            rateLimiter(limit = 10, refillPeriod = 60.seconds)
        }
    }

    install(CallLogging) {
        level = Level.INFO
        filter { call ->
            call.request.path().startsWith("/api")
        }
        format { call ->
            "[${call.request.httpMethod.value}] ${call.request.uri}"
        }
        callIdMdc(CORRELATION_ID_KEY)
    }

    install(CallId) {
        header(CORRELATION_ID_HEADER)
        generate(10, CALL_ID_DEFAULT_DICTIONARY)
    }

    install(RequestValidation) {
        validate<OppslagRequest> { toValidationResult(validateOppslagRequest(it)) }
        validate<DelbestillingRequest> { toValidationResult(validateDelbestillingRequest(it)) }
    }

    configureStatusPages()

    if (isLocal()) {
        authentication {
            tokenXMock {
                setAsDefault = false
                alwaysAuthenticated = true
                staticLevelOfAssurance = LevelOfAssurance.LEVEL_4
                staticUserPid = "12345678910"
            }
        }
    } else {
        authentication {
            azure()
            tokenX()
        }
    }
}

private fun toValidationResult(feilmeldinger: List<String>): ValidationResult {
    return if (feilmeldinger.isEmpty()) {
        ValidationResult.Valid
    } else {
        ValidationResult.Invalid(feilmeldinger)
    }
}
