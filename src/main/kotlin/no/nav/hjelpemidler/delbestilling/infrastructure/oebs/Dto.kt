package no.nav.hjelpemidler.delbestilling.infrastructure.oebs

import no.nav.hjelpemidler.delbestilling.delbestilling.Lagerstatus
import java.time.LocalDate

data class FnrDto(
    val fnr: String
)

data class UtlånPåArtnrOgSerienrRequest(
    val artnr: String,
    val serienr: String,
)

data class UtlånPåArtnrOgSerienrResponse(
    val utlån: Utlån?,
)

data class Utlån(
    val fnr: String,
    val artnr: String,
    val serienr: String,
    val utlånsDato: String,
)

data class Ordre(
    val brukersFnr: String,
    val saksnummer: String,
    val innsendernavn: String,
    val artikler: List<Artikkel>,
    val forsendelsesinfo: String, // Info til 5.17-skjema
)

data class Artikkel(
    val hmsnr: String,
    val antall: Int,
)

data class OebsPersoninfo(
    val leveringKommune: String,
)

data class Brukerpass(
    val brukerpass: Boolean,
    val kontraktNummer: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null
)

data class LagerstatusRequest(
    val hmsnrs: List<String>
)

data class LagerstatusResponse(
    val erPåLager: Boolean,
    val organisasjons_id: Int,
    val organisasjons_navn: String,
    val artikkelnummer: String,
    val artikkelid: Int,
    val fysisk: Int,
    val tilgjengeligatt: Int,
    val tilgjengeligroo: Int,
    val tilgjengelig: Int,
    val behovsmeldt: Int,
    val reservert: Int,
    val restordre: Int,
    val bestillinger: Int,
    val anmodning: Int,
    val intanmodning: Int,
    val forsyning: Int,
    val sortiment: Boolean,
    val lagervare: Boolean,
    val minmax: Boolean,
) {
    fun tilLagerstatus() = Lagerstatus(
        organisasjons_id = this.organisasjons_id,
        organisasjons_navn = this.organisasjons_navn,
        artikkelnummer = this.artikkelnummer,
        minmax = this.minmax,
        tilgjengelig = this.tilgjengelig,
    )
}