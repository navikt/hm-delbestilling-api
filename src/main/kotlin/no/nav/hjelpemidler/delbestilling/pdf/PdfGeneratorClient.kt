package no.nav.hjelpemidler.delbestilling.pdf

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readRawBytes
import io.ktor.http.ContentType
import io.ktor.http.contentType
import no.nav.hjelpemidler.delbestilling.config.AppConfig
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingTilPdf
import no.nav.hjelpemidler.http.createHttpClient

class PdfGeneratorClient(
    engine: HttpClientEngine = CIO.create(),
    private val baseUrlPdfgen: String = AppConfig.PDF_GEN_BASEURL
) {

    private val log = KotlinLogging.logger { }

    val client = createHttpClient(engine) {
        expectSuccess = true
        defaultRequest {
            accept(ContentType.Application.Pdf)
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun lagDelbestillingsbrev(delbestilling: DelbestillingTilPdf): ByteArray {
        log.info { "Lager pdf for delbestilling med ukjente deler. $delbestilling" }
        val pdfByteArray = client.post("$baseUrlPdfgen/delbestilling") { setBody(delbestilling) }.readRawBytes()

        return pdfByteArray

    }

}