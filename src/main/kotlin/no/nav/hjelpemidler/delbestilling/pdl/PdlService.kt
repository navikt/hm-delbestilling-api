package no.nav.hjelpemidler.delbestilling.pdl

import no.nav.hjelpemidler.delbestilling.exceptions.PdlRequestFailedException

class PdlService(private val pdlClient: PdlClient) {

    suspend fun hentKommunenummer(fnr: String): String {
        return pdlClient.hentKommunenummer(fnr)
    }

    suspend fun hentFornavn(fnr: String, validerAdressebeskyttelse: Boolean = true): String {
        val pdlResponse = pdlClient.hentPersonNavn(fnr, validerAdressebeskyttelse)
        val navneData = pdlResponse.data?.hentPerson?.navn?.get(0)
            ?: throw PdlRequestFailedException("PDL response mangler data")

        return navneData.fornavn
    }
}
