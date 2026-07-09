package no.nav.hjelpemidler.delbestilling.pdf

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.ContentType.Application.Pdf
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingService

private val log = KotlinLogging.logger {}

fun Route.pdfApi(
    delbestillingService: DelbestillingService,
) {
    get("/delbestilling/pdf/{saksnr}") {
        val saksnr = call.parameters.getOrFail<Long>("saksnr")

        log.info { "Forsøker å hente pdf for delbestilling med saksnummer $saksnr " }
        val pdf = delbestillingService.hentPdf(saksnr)

        log.info { "Hentet pdf for delbestilling med saksnummer $saksnr" }
        call.respondBytes(pdf, contentType = Pdf)
    }
}