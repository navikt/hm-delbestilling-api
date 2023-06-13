package no.nav.hjelpemidler.delbestilling.delbestilling

import com.fasterxml.jackson.annotation.JsonValue
import io.ktor.http.HttpStatusCode
import no.nav.hjelpemidler.delbestilling.hjelpemidler.HjelpemiddelMedDeler
import java.util.UUID

data class OppslagRequest(
    val hmsnr: String,
    val serienr: String,
)

data class OppslagResultat(
    val hjelpemiddel: HjelpemiddelMedDeler?,
    val feil: OppslagFeil? = null,
    val httpStatusCode: HttpStatusCode,
)

data class OppslagResponse(
    val hjelpemiddel: HjelpemiddelMedDeler?,
    val feil: OppslagFeil? = null,
)

enum class OppslagFeil {
    TILBYR_IKKE_HJELPEMIDDEL, INGET_UTLÅN
}

data class Hjelpemiddel(
    val hmsnr: String,
    val navn: String,
    //val deler: List<Del>,
    val type: String,
)

data class Del(
    val hmsnr: String,
    val navn: String,
    val levArtNr: String? = null,
    val img: String? = null,
    val kategori: String,
)

data class DelLinje( // TODO kan vi arve felt fra Del eller lignende?
    val navn: String,
    val hmsnr: String,
    val levArtNr: String?,
    val img: String?,
    val kategori: String,
    val antall: Int,
)

// TODO kanskje lage en wrapper?
/*
List<Delelinje>
 og
Delelinje{
  del: Del,
  antall: Int
}
 */

enum class Levering {
    TIL_XK_LAGER, TIL_SERVICE_OPPDRAG,
}

data class DelbestillingRequest(
    val delbestilling: Delbestilling,
)

data class Delbestilling(
    val id: UUID,
    val hmsnr: Hmsnr,
    val serienr: Serienr,
    val deler: List<DelLinje>,
    val levering: Levering,
)

data class DelbestillingResultat(
    val id: UUID,
    val feil: DelbestillingFeil? = null,
    val httpStatusCode: HttpStatusCode,
)

data class DelbestillingResponse(
    val id: UUID,
    val feil: DelbestillingFeil? = null,
)

enum class DelbestillingFeil {
    INGET_UTLÅN,
    ULIK_GEOGRAFISK_TILKNYTNING,
    BRUKER_IKKE_FUNNET,
    BESTILLE_TIL_SEG_SELV,
    KAN_IKKE_BESTILLE
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
