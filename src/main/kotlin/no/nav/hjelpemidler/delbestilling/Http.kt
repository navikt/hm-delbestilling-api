package no.nav.hjelpemidler.delbestilling

import io.ktor.client.request.header
import io.ktor.http.HttpMessageBuilder
import org.slf4j.MDC


const val NAV_CORRELATION_ID_KEY = "X-Correlation-ID"

fun HttpMessageBuilder.navCorrelationId(): Unit =
    header(NAV_CORRELATION_ID_KEY, MDC.get(NAV_CORRELATION_ID_KEY))