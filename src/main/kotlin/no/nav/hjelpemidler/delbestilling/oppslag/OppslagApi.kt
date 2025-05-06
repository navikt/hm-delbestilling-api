package no.nav.hjelpemidler.delbestilling.oppslag

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import no.nav.hjelpemidler.delbestilling.delbestilling.model.OppslagRequest


private val log = KotlinLogging.logger {}

fun Route.oppslagApi(
    hjelpemiddeloversikt: Hjelpemiddeloversikt,
    oppslagService: OppslagService,
) {
    get("/hjelpemiddel-titler") {
        call.respond(hjelpemiddeloversikt.hentAlleHjelpemiddelTitlerCached())
    }

    post("/oppslag") {
        val request = call.receive<OppslagRequest>()
        log.info { "/oppslag request: $request" }
        call.respond(oppslagService.slåOppHjelpemiddel(request.hmsnr, request.serienr))
    }
}

