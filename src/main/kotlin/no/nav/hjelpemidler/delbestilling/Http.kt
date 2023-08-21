package no.nav.hjelpemidler.delbestilling

import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMessageBuilder
import org.slf4j.MDC


val CORRELATION_ID_HEADER = HttpHeaders.XCorrelationId
const val CORRELATION_ID_KEY = "correlationId"

fun HttpMessageBuilder.navCorrelationId(): Unit =
    header(CORRELATION_ID_HEADER, MDC.get(CORRELATION_ID_KEY))