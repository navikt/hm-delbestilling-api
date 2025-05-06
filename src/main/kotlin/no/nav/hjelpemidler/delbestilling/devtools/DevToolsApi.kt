package no.nav.hjelpemidler.delbestilling.devtools

import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingService
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.AnmodningService
import no.nav.hjelpemidler.delbestilling.delbestilling.model.OppslagRequest
import no.nav.hjelpemidler.delbestilling.delbestilling.requireHmsnr
import no.nav.hjelpemidler.delbestilling.oppslag.OppslagService


fun Route.devtoolsApi(
    delbestillingService: DelbestillingService,
    anmodningService: AnmodningService,
    oppslagService: OppslagService,
) {
    post("/oppslag-ekstern-dev") {
        // Endepunkt for å slå opp deler til hjm. i ekstern-dev. Ignorerer serienr
        val hmsnr = requireHmsnr(call.receive<OppslagRequest>().hmsnr)
        call.respond(oppslagService.slåOppHjelpemiddel(hmsnr))
    }

    get("/finnGyldigTestbruker") {
        call.respond(delbestillingService.finnTestpersonMedTestbartUtlån())
    }

    post("/rapporter-deler-uten-dekning") {
        call.respond(delbestillingService.rapporterDelerTilAnmodning())
    }

    delete("/rapporter-deler-uten-dekning") {
        call.respond(anmodningService.markerDelerSomIkkeRapportert())
    }
}