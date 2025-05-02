package no.nav.hjelpemidler.delbestilling.infrastructure.pdl

import no.nav.hjelpemidler.delbestilling.infrastructure.monitoring.PdlResponseMissingData


class Pdl(private val client: PdlClient) {

    suspend fun hentKommunenummer(fnr: String): String {
        val response = valider(client.hentKommunenummer(fnr))
        return response.data?.hentPerson?.bostedsadresse?.get(0)?.vegadresse?.kommunenummer
            ?: throw PdlResponseMissingData("Klarte ikke å finne kommunenummer.")
    }

    suspend fun hentFornavn(fnr: String): String {
        val response = valider(client.hentPersonNavn(fnr))
        return response.data?.hentPerson?.navn?.get(0)?.fornavn
            ?: throw PdlResponseMissingData("Fant ikke fornavn.")
    }
}