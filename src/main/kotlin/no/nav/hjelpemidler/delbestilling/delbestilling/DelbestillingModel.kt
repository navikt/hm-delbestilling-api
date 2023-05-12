package no.nav.hjelpemidler.delbestilling.delbestilling

import com.fasterxml.jackson.annotation.JsonValue
import java.util.UUID

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


data class DelbestillingRequest(
    val id: UUID,
    val hmsnr: String, //Hmsnr,
    val serienr: String,//Serienr,
    val deler: List<DelLinje>
)

data class DelLinje(
    val hmsnr: String,//Hmsnr,
    val antall: Int//Antall
)

@JvmInline
value class Hmsnr( val hmsnr: String) {
    init {
        require(hmsnr.length == 6) { "hmsnr må ha lengde 6" }
        require(hmsnr.all { it.isDigit() }) { "hmsnr må bestå av siffer" }
    }
}

@JvmInline
value class Serienr(private val serienr: String) {
    init {
        require(serienr.length < 10) { "serienr max 10 siffer" } // TODO hva er begrensingene på serienr?
        require(serienr.all { it.isDigit() }) { "serienr må bestå av siffer" }
    }
}

@JvmInline
value class Antall(private val antall: Int) {
    init {
        require(antall > 0) { "antall må være >0" }
        require(antall < 20) { "antall må være <20" }
    }
}