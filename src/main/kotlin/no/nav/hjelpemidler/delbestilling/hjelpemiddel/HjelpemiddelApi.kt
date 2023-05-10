package no.nav.hjelpemidler.delbestilling.hjelpemiddel

import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.hjelpemiddelOppslagApi() {
    post("/oppslag") {
        val request = call.receive<OppslagRequest>()
        val hjelpemiddel = hjelpemidler[request.artNr]
        val serieNrKobletMotBuker = request.serieNr != "000000"

        call.respond(OppslagResponse(hjelpemiddel, serieNrKobletMotBuker))
    }
}

data class OppslagRequest(
    val artNr: String,
    val serieNr: String,
)

data class OppslagResponse(
    val hjelpemiddel: Hjelpemiddel?,
    val serieNrKobletMotBuker: Boolean
)

data class Hjelpemiddel(
    val navn: String,
    val hmsnr: String,
    val deler: List<Del>
)

data class Del(
    val navn: String,
    val beskrivelse: String,
    val hmsnr: String,
    val levArtNr: String,
    val img: String,
    val kategori: String,
)

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
