package no.nav.hjelpemidler.delbestilling.infrastructure

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMessageBuilder
import io.ktor.http.contentType
import no.nav.hjelpemidler.delbestilling.isProd
import no.nav.hjelpemidler.http.createHttpClient
import org.slf4j.MDC


val CORRELATION_ID_HEADER = HttpHeaders.XCorrelationId
const val CORRELATION_ID_KEY = "correlationId"

fun HttpMessageBuilder.navCorrelationId(): Unit =
    header(CORRELATION_ID_HEADER, MDC.get(CORRELATION_ID_KEY))

fun defaultHttpClient(engine: HttpClientEngine = CIO.create()) = createHttpClient(engine = engine) {
    expectSuccess = true

    install(HttpRequestRetry) {
        retryOnExceptionOrServerErrors(maxRetries = 5)
        exponentialDelay()
    }

    install(Logging) {
        level = if (isProd()) LogLevel.INFO else LogLevel.BODY
    }

    defaultRequest {
        accept(ContentType.Application.Json)
        contentType(ContentType.Application.Json)
    }
}