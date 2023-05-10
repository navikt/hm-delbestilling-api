package no.nav.hjelpemidler.delbestilling.delbestilling

import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging

private val log = KotlinLogging.logger {}

fun Route.delbestillingApi() {
    post("/oppslag") {
        log.info { "kall til /oppslag" }
        val request = call.receive<OppslagRequest>()
        log.info { "request: $request" }
        val hjelpemiddel = hjelpemidler[request.artNr]
        val serieNrKobletMotBuker = request.serieNr != "000000"

        call.respond(OppslagResponse(hjelpemiddel, serieNrKobletMotBuker))
    }
}


private val hjelpemidler = mapOf(
    "111111" to Hjelpemiddel(
        navn = "Panthera S3",
        hmsnr = "111111",
        deler = listOf(
            Del(
                navn = "Brems S3 høyre",
                beskrivelse = "Standard brems for S3 og U3",
                hmsnr = "252264",
                levArtNr = "4630002",
                img = "",
                kategori = "Brems"
            ),
            Del(
                navn = "Brems S3 venstre",
                beskrivelse = "Standard brems for S3 og U3",
                hmsnr = "252265",
                levArtNr = "4630001",
                img = "",
                kategori = "Brems"
            )
        )
    ),
    "222222" to Hjelpemiddel(
        navn = "Panthera U3",
        hmsnr = "111111",
        deler = listOf(
            Del(
                navn = "Brems S3 høyre",
                beskrivelse = "Standard brems for S3 og U3",
                hmsnr = "252264",
                levArtNr = "4630002",
                img = "",
                kategori = "Brems"
            ),
            Del(
                navn = "Schwalbe Marathon Plus 24 x 1",
                beskrivelse = "Punkteringsbeskyttet dekk til S3/U3 Light.",
                hmsnr = "252265",
                levArtNr = "4630001",
                img = "",
                kategori = "Dekk"
            )
        )
    )
)
