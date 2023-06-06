package no.nav.hjelpemidler.delbestilling.oebs


data class UtlånPåArtnrOgSerienrRequest(
    val artnr: String,
    val serienr: String
)

data class UtlånPåArtnrOgSerienrResponse(
    val utlån: Utlån?
)


data class Utlån(
    val fnr: String,
    val artnr: String,
    val serienr: String,
    val utlånsDato: String,
)

data class OpprettBestillingsordreRequest(
    val brukerFnr: String,
    val saksnummer: String,
    val bestillersNavn: String,
    val deler: List<Artikkel>
)

data class Artikkel(
    val hmsnr: String,
    val antall: Int,
)