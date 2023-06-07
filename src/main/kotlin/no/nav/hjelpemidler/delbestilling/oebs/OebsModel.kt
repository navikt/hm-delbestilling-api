package no.nav.hjelpemidler.delbestilling.oebs

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
    // TODO: Egen dataklasse med bedre navn?
    val brukersFnr: String,
    val saksnummer: String,
    val innsendernavn: String,
    val artikler: List<Artikkel>
)

data class Artikkel(
    val hmsnr: String,
    val antall: Int,
)
