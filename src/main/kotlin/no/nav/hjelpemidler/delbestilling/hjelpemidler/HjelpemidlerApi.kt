package no.nav.hjelpemidler.delbestilling.hjelpemidler

import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.DELER
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hjmType2Deler

fun Route.hjelpemiddelApi(
    hjelpemidlerService: HjelpemidlerService
) {
    get("/hjelpemidler") {
        val hjelpemidlerMedDeler = hjelpemidlerService.hentAlleHjelpemidlerMedDeler()
        call.respond(hjelpemidlerMedDeler)
    }

    get("/deler") {
        call.respond(hjmType2Deler)
    }

    get("/deler-v2") {
        call.respond(
            mapOf(
                "oppdatert" to DELER.values.maxBy { it.datoLagtTil }.datoLagtTil,
                "deler" to hjmType2Deler,
            )
        )
    }

}
