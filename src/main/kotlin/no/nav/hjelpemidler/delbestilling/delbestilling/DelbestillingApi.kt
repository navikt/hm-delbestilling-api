package no.nav.hjelpemidler.delbestilling.delbestilling

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.util.getOrFail
import mu.KotlinLogging
import no.nav.hjelpemidler.delbestilling.tokenXUser
import java.time.LocalDate

private val log = KotlinLogging.logger {}

fun Route.delbestillingApiPublic(
    delbestillingService: DelbestillingService,
) {
    post("/oppslag") {
        try {
            val request = call.receive<OppslagRequest>()
            log.info { "/oppslag request: $request" }

            val resultat = delbestillingService.slåOppHjelpemiddel(request.hmsnr, request.serienr)
            if (resultat.feil != null) {
                log.info { "Oppslag på hmsnr:${request.hmsnr} serienr:${request.serienr} returnerte feilkode:${resultat.feil}" }
            }

            val oppslagResponse = OppslagResponse(resultat.hjelpemiddel, resultat.feil)

            call.respond(resultat.httpStatusCode, oppslagResponse)
        } catch (e: Exception) {
            log.error(e) { "Klarte ikke gjøre oppslag" }
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
}

fun Route.delbestillingApiAuthenticated(
    delbestillingService: DelbestillingService,
) {
    post("/delbestilling") {
        try {
            val request = call.receive<DelbestillingRequest>()
            val bestiller = call.tokenXUser()

            val resultat = delbestillingService.opprettDelbestilling(request, bestiller.ident, bestiller.tokenString)

            log.info { "opprettDelbestilling resultat: saksnummer=${resultat.saksnummer}, feil=${resultat.feil}" }

            val statusKode = when (resultat.feil) {
                DelbestillingFeil.INGET_UTLÅN -> HttpStatusCode.NotFound
                DelbestillingFeil.KAN_IKKE_BESTILLE -> HttpStatusCode.NotFound
                DelbestillingFeil.BRUKER_IKKE_FUNNET -> HttpStatusCode.NotFound
                DelbestillingFeil.BESTILLE_TIL_SEG_SELV -> HttpStatusCode.Forbidden
                DelbestillingFeil.ULIK_GEOGRAFISK_TILKNYTNING -> HttpStatusCode.Forbidden
                DelbestillingFeil.ULIK_ADRESSE_PDL_OEBS -> HttpStatusCode.Forbidden
                DelbestillingFeil.FOR_MANGE_BESTILLINGER_SISTE_24_TIMER -> HttpStatusCode.Forbidden
                DelbestillingFeil.HAR_IKKE_FORELDREANSVAR -> HttpStatusCode.Forbidden
                null -> HttpStatusCode.Created
            }

            call.respond(statusKode, resultat)
        } catch (e: Exception) {
            log.error(e) { "Innsending av bestilling feilet" }
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

    get("/delbestilling") {
        val bestillerFnr = call.tokenXUser().ident
        val delbestillinger = delbestillingService.hentDelbestillinger(bestillerFnr)
        call.respond(delbestillinger)
    }
}

fun Route.azureRoutes(
    delbestillingService: DelbestillingService,
) {
    put("/delbestilling/status/v2/{id}") {
        val id = call.parameters.getOrFail<Long>("id")
        val (status, oebsOrdrenummer) = call.receive<StatusOppdateringDto>()
        log.info { "Oppdaterer status for delbestilling $id (hmdel_$id) til status $status" }
        delbestillingService.oppdaterStatus(id, status, oebsOrdrenummer)
        call.respond(HttpStatusCode.OK)
        log.info { "Status for delbestilling $id (hmdel_$id) oppdatert OK" }
    }

    put("/delbestilling/status/dellinje/{oebsOrdrenummer}") {
        val oebsOrdrenummer = call.parameters.getOrFail<String>("oebsOrdrenummer")
        val (status, hmsnr, datoOppdatert) = call.receive<DellinjeStatusOppdateringDto>()
        delbestillingService.oppdaterDellinjeStatus(oebsOrdrenummer, status, hmsnr, datoOppdatert)
        call.respond(HttpStatusCode.OK)
    }
}


private data class StatusOppdateringDto(
    val status: Status,
    val oebsOrdrenummer: String,
)

private data class DellinjeStatusOppdateringDto(
    val status: DellinjeStatus,
    val hmsnr: Hmsnr,
    val datoOppdatert: LocalDate,
)