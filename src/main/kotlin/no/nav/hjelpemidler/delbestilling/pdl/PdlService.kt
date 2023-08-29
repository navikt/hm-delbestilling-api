package no.nav.hjelpemidler.delbestilling.pdl

import no.nav.hjelpemidler.delbestilling.exceptions.PdlRequestFailedException

class PdlService(private val pdlClient: PdlClient) {

    suspend fun hentKommunenummer(fnr: String): String {
        return pdlClient.hentKommunenummer(fnr)
    }

    suspend fun hentPersonNavn(fnr: String, validerAdressebeskyttelse: Boolean = true): String {
        val pdlResponse = pdlClient.hentPersonNavn(fnr, validerAdressebeskyttelse)
        val navneData = pdlResponse.data?.hentPerson?.navn?.get(0)
            ?: throw PdlRequestFailedException("PDL response mangler data")
        val fornavn = if (navneData.mellomnavn.isNullOrBlank()) {
            navneData.fornavn
        } else {
            "${navneData.fornavn} ${navneData.mellomnavn}"
        }
        return "$fornavn ${navneData.etternavn}"
    }

    suspend fun harGodkjentForeldreansvarForPerson(bestillerFnr: String, brukersFnr: String): Boolean {
        val pdlRespons = pdlClient.hentForeldreansvar(bestillerFnr, true)
        val foreldreansvar =
            pdlRespons.data?.hentPerson?.foreldreansvar ?: throw PdlRequestFailedException("PDL response mangler data")

        return foreldreansvar.any { it.ansvarssubjekt == brukersFnr }
    }
}
