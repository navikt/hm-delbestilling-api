package no.nav.hjelpemidler.delbestilling.infrastructure.geografi


interface OppslagClientInterface {
    suspend fun hentKommune(kommunenr: String): KommuneDto
    suspend fun hentPoststed(postnummer: String): PoststedDto
}

