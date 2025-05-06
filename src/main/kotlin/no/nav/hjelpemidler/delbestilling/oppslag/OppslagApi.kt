package no.nav.hjelpemidler.delbestilling.oppslag

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import no.nav.hjelpemidler.delbestilling.config.isDev
import no.nav.hjelpemidler.delbestilling.delbestilling.model.OppslagRequest
import no.nav.hjelpemidler.delbestilling.delbestilling.model.OppslagResponse
import no.nav.hjelpemidler.delbestilling.delbestilling.requireHmsnr


private val log = KotlinLogging.logger {}

fun Route.oppslagApi(
    hjelpemiddeloversikt: Hjelpemiddeloversikt,
    oppslagService: OppslagService,
) {
    get("/hjelpemiddel-titler") {
        call.respond(hjelpemiddeloversikt.hentAlleHjelpemiddelTitlerCached())
    }

    post("/oppslag") {
        try {
            val request = call.receive<OppslagRequest>()
            log.info { "/oppslag request: $request" }

            val resultat = oppslagService.slåOppHjelpemiddel(request.hmsnr, request.serienr)
            if (resultat.feil != null) {
                log.info { "Oppslag på hmsnr:${request.hmsnr} serienr:${request.serienr} returnerte feilkode:${resultat.feil}" }
            }

            val oppslagResponse = OppslagResponse(resultat.hjelpemiddel, resultat.feil, resultat.piloter)

            call.respond(resultat.httpStatusCode, oppslagResponse)
        } catch (e: Exception) {
            log.error(e) { "Klarte ikke gjøre oppslag" }
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
}

