package no.nav.hjelpemidler.delbestilling.infrastructure.pdl

import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger {}

class Pdl(private val client: PdlClientInterface) {

    suspend fun hentKommunenummer(fnr: String): String {
        val kommunenummer = try {
            val response = valider(client.hentKommunenummer(fnr))
            response.data?.hentPerson?.bostedsadresse?.get(0)?.vegadresse?.kommunenummer
                ?: throw PdlResponseMissingData("Kommunenummer mangler i PDL-data")
        } catch (e:Exception) {
            log.error(e) { "Klarte ikke å hente kommunenummer" }
            throw e
        }

        return kommunenummer
    }

    suspend fun hentFornavn(fnr: String): String {
        val response = valider(client.hentPersonNavn(fnr))
        return response.data?.hentPerson?.navn?.get(0)?.fornavn
            ?: throw PdlResponseMissingData("Fant ikke fornavn.")
    }
}