package no.nav.hjelpemidler.delbestilling.delbestilling

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import no.nav.hjelpemidler.delbestilling.infrastructure.CORRELATION_ID_HEADER
import no.nav.hjelpemidler.delbestilling.infrastructure.security.delbestillerRolleKey
import no.nav.hjelpemidler.delbestilling.infrastructure.security.tokenXUser
import no.nav.hjelpemidler.delbestilling.infrastructure.slack.Slack
import no.nav.hjelpemidler.delbestilling.oppslag.OppslagRequest

private val log = KotlinLogging.logger {}

fun Route.delbestillingApiAuthenticated(
    delbestillingService: DelbestillingService,
    slack: Slack,
) {
    post("/delbestilling") {
        try {
            val request = call.receive<DelbestillingRequest>()
            val delbestillerFnr = call.tokenXUser().ident
            val delbestillerRolle = call.attributes[delbestillerRolleKey]

            val resultat = delbestillingService.opprettDelbestilling(request, delbestillerFnr, delbestillerRolle)

            log.info { "opprettDelbestilling resultat: saksnummer=${resultat.saksnummer}, feil=${resultat.feil}" }

            val statusKode = when (resultat.feil) {
                DelbestillingFeil.INGET_UTLÅN -> HttpStatusCode.NotFound
                DelbestillingFeil.KAN_IKKE_BESTILLE -> HttpStatusCode.NotFound
                DelbestillingFeil.BRUKER_IKKE_FUNNET -> HttpStatusCode.NotFound
                DelbestillingFeil.BESTILLE_TIL_SEG_SELV -> HttpStatusCode.Forbidden
                DelbestillingFeil.ULIK_GEOGRAFISK_TILKNYTNING -> HttpStatusCode.Forbidden
                DelbestillingFeil.ULIK_ADRESSE_PDL_OEBS -> HttpStatusCode.Forbidden
                DelbestillingFeil.FOR_MANGE_BESTILLINGER_SISTE_24_TIMER -> HttpStatusCode.Forbidden
                null -> HttpStatusCode.Created
            }

            call.respond(statusKode, resultat)
        } catch (e: Exception) {
            log.error(e) { "Innsending av bestilling feilet" }
            slack.varsleOmInnsendingFeilet(call.request.headers[CORRELATION_ID_HEADER] ?: "UKJENT")
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

    post("/xk-lager") {
        try {
            val request = call.receive<OppslagRequest>()
            log.info { "/xk-lager request: $request" }
            val xklager = XKLagerResponse(delbestillingService.sjekkXKLager(request.hmsnr, request.serienr))
            log.info { "/xk-lager response: $xklager" }
            call.respond(xklager)

        } catch (e: Exception) {
            log.error(e) { "Henting av XKLager feilet" }
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

    get("/delbestilling") {
        val bestillerFnr = call.tokenXUser().ident
        val delbestillinger = delbestillingService.hentDelbestillinger(bestillerFnr)
        call.respond(delbestillinger)
    }

}
