package no.nav.hjelpemidler.delbestilling.oebs

import java.time.LocalDate

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

data class OpprettBestillingsordreRequest(
    val brukersFnr: String,
    val saksnummer: String,
    val innsendernavn: String,
    val artikler: List<Artikkel>,
    val forsendelsesinfo: String,
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