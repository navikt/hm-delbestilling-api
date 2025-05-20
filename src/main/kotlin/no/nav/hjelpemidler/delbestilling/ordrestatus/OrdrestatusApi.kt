package no.nav.hjelpemidler.delbestilling.ordrestatus

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.put
import io.ktor.server.util.getOrFail

private val log = KotlinLogging.logger {}

fun Route.ordrestatusRoutes(
    delbestillingStatusService: DelbestillingStatusService,
) {
    put("/delbestilling/status/v2/{id}") {
        val id = call.parameters.getOrFail<Long>("id")
        val (status, oebsOrdrenummer) = call.receive<StatusOppdateringRequest>()
        log.info { "Oppdaterer status for delbestilling $id (hmdel_$id) til status $status" }


        delbestillingStatusService.oppdaterStatus(id, status, oebsOrdrenummer)
        call.respond(HttpStatusCode.OK)
        log.info { "Status for delbestilling $id (hmdel_$id) oppdatert OK" }
    }

    put("/delbestilling/status/dellinje/{oebsOrdrenummer}") {
        val oebsOrdrenummer = call.parameters.getOrFail<String>("oebsOrdrenummer")
        val (status, hmsnr, datoOppdatert) = call.receive<DellinjeStatusOppdateringRequest>()

        delbestillingStatusService.oppdaterDellinjeStatus(oebsOrdrenummer, status, hmsnr, datoOppdatert)

        call.respond(HttpStatusCode.OK)
    }
}
