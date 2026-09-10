package no.nav.hjelpemidler.delbestilling.devtools

import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.HttpStatusCode
import no.nav.hjelpemidler.delbestilling.config.isDev
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingRequest
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingService
import no.nav.hjelpemidler.delbestilling.delbestilling.requireHmsnr
import no.nav.hjelpemidler.delbestilling.delbestilling.validateDelbestillingRequest
import no.nav.hjelpemidler.delbestilling.rapportering.klargjorte.KlargjorteDelbestillingerService


fun Route.devtoolsApi(
    devTools: DevTools,
    delbestillingService: DelbestillingService,
    klargjorteDelbestillingerService: KlargjorteDelbestillingerService,
) {

    if (!isDev()) return

    post("/oppslag-ekstern-dev-deler") {
        // Endepunkt for å slå opp deler til hjm. i ekstern-dev. Ignorerer serienr
        val request = call.receive<OppslagRequestDev>()
        val hmsnr = requireHmsnr(request.hmsnr)
        val serienr = request.serienr
        val brukernr = request.brukernr
        call.respond(devTools.slåOppHjelpemiddelMedFakeLagerstatus(hmsnr, serienr, brukernr))
    }

    post("/oppslag-ekstern-dev-hjelpemiddel") {
        // Endepunkt for å slå opp deler til hjm. i ekstern-dev. Ignorerer serienr
        val request = call.receive<OppslagRequestHmsnr>()
        val hmsnr = requireHmsnr(request.hmsnr)
        call.respond(devTools.slåOppHjelpemiddel(hmsnr))
    }

    get("/finnGyldigTestbruker") {
        call.respond(devTools.finnTestpersonMedTestbartUtlån())
    }

    post("/rapporter-deler-uten-dekning") {
        call.respond(delbestillingService.rapporterDelerTilAnmodning())
    }

    post("/rapporter-klargjorte-delbestillinger") {
        call.respond(klargjorteDelbestillingerService.rapporterKlargjorteDelbestillinger(30))
    }

    delete("/rapporter-deler-uten-dekning") {
        call.respond(devTools.markerDelerSomIkkeBehandlet())
    }

    post("/test-email") {
        devTools.sendTestMail()
        call.respond("OK")
    }

    post("/test-email-manuell-delbestilling") {
        val mottaker = call.request.queryParameters["mottaker"]
        if (mottaker == null || !mottaker.endsWith("@nav.no", ignoreCase = true)) {
            call.respond(HttpStatusCode.BadRequest, mapOf("feil" to "Mottaker må være en @nav.no-adresse"))
            return@post
        }

        val request = call.receive<DelbestillingRequest>()
        val valideringsfeil = validateDelbestillingRequest(request)
        if (request.delbestilling.ukjenteDeler.isEmpty()) {
            call.respond(HttpStatusCode.BadRequest, mapOf("feil" to "Testbestillingen må inneholde en ukjent del"))
            return@post
        }
        if (valideringsfeil.isNotEmpty()) {
            call.respond(HttpStatusCode.BadRequest, mapOf("feil" to valideringsfeil))
            return@post
        }

        devTools.sendTestMailManuellDelbestilling(mottaker, request)
        call.respond("OK")
    }
}

private data class OppslagRequestHmsnr(
    val hmsnr: String,
)

private data class OppslagRequestDev(
    val hmsnr: String,
    val serienr: String?,
    val brukernr: String?,
)