package no.nav.hjelpemidler.delbestilling.delbestilling

import com.fasterxml.jackson.annotation.JsonValue
import java.util.UUID

data class OppslagRequest(
    val hmsnr: String,
    val serienr: String,
)

data class OppslagResponse(
    val hjelpemiddel: Hjelpemiddel?,
    val feil: OppslagFeil?,
)

enum class OppslagFeil {
    TILBYR_IKKE_HJELPEMIDDEL, INGET_UTLÅN
}

data class Hjelpemiddel(
    val navn: String,
    val hmsnr: String,
    val deler: List<Del>,
)

data class Del(
    val navn: String,
    val beskrivelse: String,
    val hmsnr: String,
    val levArtNr: String,
    val img: String,
    val kategori: String,
)

data class DelLinje( // TODO kan vi arve felt fra Del eller lignende?
    val navn: String,
    val beskrivelse: String,
    val hmsnr: String,
    val levArtNr: String,
    val img: String,
    val kategori: String,
    val antall: Int,
)

data class DelbestillingRequest(
    val delbestilling: Delbestilling,
)

data class Delbestilling(
    val id: UUID,
    val hmsnr: Hmsnr,
    val serienr: Serienr,
    val deler: List<DelLinje>,
)

data class DelbestillingResponse(
    val id: UUID,
    val feil: DelbestillingFeil?,
)

enum class DelbestillingFeil {
    INGET_UTLÅN, ULIK_GEOGRAFISK_TILKNYTNING, BRUKER_IKKE_FUNNET
}

/* TODO: Vurder om vi skal bruke https://ktor.io/docs/request-validation.html#configure
    for validering av innkommende data
*/
data class Hmsnr(@get:JsonValue val value: String) {
    init {
        require(value.length == 6) { "hmsnr må ha lengde 6" }
        require(value.all { it.isDigit() }) { "hmsnr må bestå av siffer" }
    }
}

data class Serienr(@get:JsonValue val value: String) {
    init {
        require(value.length < 10) { "serienr max 10 siffer" } // TODO hva er begrensingene på serienr?
        require(value.all { it.isDigit() }) { "serienr må bestå av siffer" }
    }
}

data class Antall(@get:JsonValue val value: Int) {
    init {
        require(value > 0) { "antall må være >0" }
        require(value < 20) { "antall må være <20" }
    }
}
