package no.nav.hjelpemidler.delbestilling.devtools

import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import no.nav.hjelpemidler.delbestilling.config.isDev
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingService
import no.nav.hjelpemidler.delbestilling.delbestilling.requireHmsnr
import no.nav.hjelpemidler.delbestilling.delbestilling.requireSerienr
import no.nav.hjelpemidler.delbestilling.oppslag.OppslagRequest


fun Route.devtoolsApi(
    devTools: DevTools,
    delbestillingService: DelbestillingService,
) {

    if (!isDev()) return

    post("/oppslag-ekstern-dev") {
        // Endepunkt for å slå opp deler til hjm. i ekstern-dev. Ignorerer serienr
        val request = call.receive<OppslagRequest>()
        val hmsnr = requireHmsnr(request.hmsnr)
        val serienr = requireSerienr(request.serienr)
        call.respond(devTools.slåOppHjelpemiddelMedFakeLagerstatus(hmsnr, serienr))
    }

    get("/finnGyldigTestbruker") {
        call.respond(devTools.finnTestpersonMedTestbartUtlån())
    }

    post("/rapporter-deler-uten-dekning") {
        call.respond(delbestillingService.rapporterDelerTilAnmodning())
    }

    post("/rapporter-klargjorte-delbestillinger") {
        call.respond(delbestillingService.rapporterKlargjorteDelbestillinger(30))
    }

    delete("/rapporter-deler-uten-dekning") {
        call.respond(devTools.markerDelerSomIkkeBehandlet())
    }

    post("/test-email") {
        devTools.sendTestMail()
        call.respond("OK")
    }
}