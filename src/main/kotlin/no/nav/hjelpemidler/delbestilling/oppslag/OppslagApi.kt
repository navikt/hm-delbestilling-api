package no.nav.hjelpemidler.delbestilling.oppslag

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post


private val log = KotlinLogging.logger {}

fun Route.oppslagApi(
    hjelpemiddeloversikt: Hjelpemiddeloversikt,
    oppslagService: OppslagService,
    finnDelerTilHjelpemiddel: FinnDelerTilHjelpemiddel,
) {
    get("/hjelpemiddel-titler") {
        call.respond(hjelpemiddeloversikt.hentAlleHjelpemiddelTitlerCached())
    }

    post("/deler-til-hjelpemiddel") {
        val hmsnr = call.receive<DelerTilHjelpemiddelRequest>().hmsnr
        call.respond(finnDelerTilHjelpemiddel(hmsnr))
    }

    post("/oppslag") {
        val request = call.receive<OppslagRequest>()
        log.info { "/oppslag request: $request" }
        call.respond(oppslagService.slåOppHjelpemiddel(request.hmsnr, request.serienr))
    }
}

