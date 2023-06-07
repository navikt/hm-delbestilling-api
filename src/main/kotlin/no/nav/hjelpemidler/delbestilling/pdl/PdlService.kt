package no.nav.hjelpemidler.delbestilling.pdl

import no.nav.hjelpemidler.delbestilling.exceptions.PdlRequestFailedException

class PdlService(val pdlClient: PdlClient) {

    suspend fun hentKommunenummer(fnr: String): String {
        return pdlClient.hentKommunenummer(fnr)
    }

    suspend fun hentPersonNavn(fnr: String, validerAdressebeskyttelse: Boolean = true): String {
        val pdlResponse = pdlClient.hentPersonNavn(fnr, validerAdressebeskyttelse)
        val navneData = pdlResponse.data?.hentPerson?.navn?.get(0)
            ?: throw PdlRequestFailedException("PDL response mangler data")
        val mellomnavn = navneData.mellomnavn ?: ""
        return "${navneData.fornavn} $mellomnavn ${navneData.etternavn}"
    }
}
