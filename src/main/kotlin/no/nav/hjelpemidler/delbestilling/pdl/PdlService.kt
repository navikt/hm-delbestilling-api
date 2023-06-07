package no.nav.hjelpemidler.delbestilling.pdl

class PdlService(val pdlClient: PdlClient) {

    suspend fun hentKommunenummer(fnr: String): String {
        return pdlClient.hentKommunenummer(fnr)
    }

    suspend fun hentPersonNavn(fnr: String): String {
        return pdlClient.hentPersonNavn(fnr)
    }
}
