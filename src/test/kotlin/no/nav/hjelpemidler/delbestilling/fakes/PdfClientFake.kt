package no.nav.hjelpemidler.delbestilling.fakes

import io.ktor.client.statement.HttpResponse
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.PdlPersonResponse

class PdfClientFake {

    suspend fun lagDelbestillingsbrev(fnr: String): ByteArray {
        return ByteArray(10) {it.toByte()}
    }
}