package no.nav.hjelpemidler.delbestilling.infrastructure.geografi

data class KommuneDto(
    val fylkesnummer: String,
    val fylkesnavn: String,
    val kommunenummer: String,
    val kommunenavn: String,
)

data class PoststedDto(
    val postnummer: String,
    val poststed: String,
    val kommunenummer: String,
    val kommunenavn: String,
)