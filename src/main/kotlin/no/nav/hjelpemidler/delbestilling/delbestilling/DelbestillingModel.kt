package no.nav.hjelpemidler.delbestilling.delbestilling

import io.ktor.http.HttpStatusCode
import no.nav.hjelpemidler.delbestilling.hjelpemidler.defaultAntall
import java.time.LocalDate
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
) {
    init {
        hjelpemiddel?.deler?.forEach {
            if (it.lagerstatus == null) {
                error{"Del $it på hjelpemiddel ${hjelpemiddel.hmsnr} mangler lagerstatus, kan ikke returnere resultat"}
            }
        }
    }
}

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

data class XKLagerResponse (
    val xkLager: Boolean,
)

data class HjelpemiddelMedDeler(
    val navn: String,
    val hmsnr: String,
    var deler: List<Del>, // TODO gjør om til val. Da kan også antallKategorier gjøres om til val.
) {
    fun antallKategorier(): Int = deler.distinctBy { it.kategori }.size
}

data class Del(
    val hmsnr: Hmsnr,
    val navn: String,
    val levArtNr: String? = null,
    val kategori: String,
    val defaultAntall: Int = defaultAntall(kategori),
    val maksAntall: Int, // TODO kan ofte utlede maksAntall fra kategori også
    val img: String? = null, // Skal deprecates til fordel for imgs
    val imgs: List<String> = emptyList(),
    val datoLagtTil: LocalDate? = null,
    var lagerstatus: Lagerstatus? = null, // TODO: denne bør kanskje ikke være nullable?
    val kilde: Kilde? = Kilde.MANUELL_LISTE,
)

enum class Kilde {
    GRUNNDATA,
    MANUELL_LISTE
}

data class Lagerstatus(
    val organisasjons_id: Int,
    val organisasjons_navn: String,
    val artikkelnummer: String,
    val minmax: Boolean,
    val tilgjengelig: Int,
)

data class DelLinje(
    val del: Del,
    val antall: Int,
    val status: DellinjeStatus? = null,
    val datoSkipningsbekreftet: LocalDate? = null,
    val forventetLeveringsdato: LocalDate? = null,
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
    val harOpplæringPåBatteri: Boolean?,
    val navn: String?,
)

data class DelbestillingResultat(
    val id: UUID,
    val feil: DelbestillingFeil? = null,
    val saksnummer: Long? = null,
    val delbestillingSak: DelbestillingSak? = null,
)

enum class DelbestillingFeil {
    INGET_UTLÅN,
    ULIK_GEOGRAFISK_TILKNYTNING,
    BRUKER_IKKE_FUNNET,
    BESTILLE_TIL_SEG_SELV,
    KAN_IKKE_BESTILLE,
    ULIK_ADRESSE_PDL_OEBS,
    FOR_MANGE_BESTILLINGER_SISTE_24_TIMER,
}

data class DelbestillingSak(
    val saksnummer: Long,
    val delbestilling: Delbestilling,
    val opprettet: LocalDateTime,
    val status: Status,
    val sistOppdatert: LocalDateTime,
    val oebsOrdrenummer: String?,
)

typealias Hmsnr = String
typealias Serienr = String

enum class BestillerType {
    KOMMUNAL, IKKE_KOMMUNAL, BRUKERPASS
}