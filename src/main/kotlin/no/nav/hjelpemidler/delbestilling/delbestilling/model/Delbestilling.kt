package no.nav.hjelpemidler.delbestilling.delbestilling.model

import no.nav.hjelpemidler.delbestilling.oppslag.legacy.defaultAntall
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class XKLagerResponse (
    val xkLager: Boolean,
)

data class Del(
    val hmsnr: Hmsnr,
    val navn: String,
    val levArtNr: String? = null,
    val kategori: String,
    val defaultAntall: Int = defaultAntall(kategori),
    val maksAntall: Int,
    val imgs: List<String> = emptyList(),
    var lagerstatus: Lagerstatus? = null,
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
    val antallDelerPåLager: Int,
)

data class DelLinje(
    val del: Del,
    val antall: Int,
    val status: DellinjeStatus? = null,
    val datoSkipningsbekreftet: LocalDate? = null,
    val forventetLeveringsdato: LocalDate? = null,
    val lagerstatusPåBestillingstidspunkt: Lagerstatus? = null
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
    val brukersKommunenummer: String,
    val brukersKommunenavn: String,
)

typealias Hmsnr = String
typealias Serienr = String

enum class BestillerType {
    KOMMUNAL, IKKE_KOMMUNAL, BRUKERPASS
}