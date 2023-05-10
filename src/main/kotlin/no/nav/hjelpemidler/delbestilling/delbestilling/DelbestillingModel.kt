package no.nav.hjelpemidler.delbestilling.delbestilling

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
