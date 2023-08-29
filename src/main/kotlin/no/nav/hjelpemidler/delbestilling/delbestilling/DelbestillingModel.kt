package no.nav.hjelpemidler.delbestilling.delbestilling

import io.ktor.http.HttpStatusCode
import no.nav.hjelpemidler.delbestilling.hjelpemidler.HjelpemiddelMedDeler
import java.time.LocalDateTime
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

data class AlleHjelpemidlerMedDelerResultat(
    val hjelpemidlerMedDeler: List<HjelpemiddelMedDeler>,
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
    val type: String,
)

data class Del(
    val hmsnr: String,
    val navn: String,
    val levArtNr: String? = null,
    val img: String? = null,
    val kategori: String,
    val maksAntall: Int,
)

data class DelLinje(
    val del: Del,
    val antall: Int,
)

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
    val rolle: Rolle
)

enum class Rolle {
    TEKNIKER, BRUKERPASS
}

data class DelbestillingResultat(
    val id: UUID,
    val feil: DelbestillingFeil? = null,
    val saksnummer: Long? = null,
)

enum class DelbestillingFeil {
    INGET_UTLÅN,
    ULIK_GEOGRAFISK_TILKNYTNING,
    BRUKER_IKKE_FUNNET,
    BESTILLE_TIL_SEG_SELV,
    KAN_IKKE_BESTILLE,
    ULIK_ADRESSE_PDL_OEBS,
    FOR_MANGE_BESTILLINGER_SISTE_24_TIMER,
    INGEN_GODKJENT_RELASJON,
}

data class LagretDelbestilling(
    val saksnummer: Long,
    val delbestilling: Delbestilling,
    val opprettet: LocalDateTime,
    val status: Status,
    val sistOppdatert: LocalDateTime,
)

typealias Hmsnr = String
typealias Serienr = String
