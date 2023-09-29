package no.nav.hjelpemidler.delbestilling.oppslag

class OppslagService(private val oppslagClient: OppslagClient) {
    suspend fun hentKommune(kommunenr: String): KommuneDto {
        try {
            return oppslagClient.hentKommune(kommunenr)
        } catch (e: Exception) {
            throw e
        }
    }
}
