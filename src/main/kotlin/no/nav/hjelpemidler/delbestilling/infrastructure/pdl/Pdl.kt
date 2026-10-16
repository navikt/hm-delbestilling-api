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
        val fornavn = try {
            val response = valider(client.hentPersonNavn(fnr))
            response.data?.hentPerson?.navn?.get(0)?.fornavn
                ?: throw PdlResponseMissingData("Fornavn mangler i PDL-data")
        } catch (e:Exception) {
            log.error(e) { "Klarte ikke å hente fornavn" }
            throw e
        }

        return fornavn

    }

    suspend fun hentFornavnOgEtternavnsinitial(fnr: String): String {
        val navn = try {
            val response = valider(client.hentPersonNavn(fnr))
            response.data?.hentPerson?.navn?.get(0)?.let { navn ->
                "${navn.fornavn} ${navn.etternavn.first()}"
            } ?: throw PdlResponseMissingData("Navn mangler i PDL-data")
        } catch (e: Exception) {
            log.error(e) { "Klarte ikke å hente navn" }
            throw e
        }
        return navn
    }

    suspend fun hentNavn(fnr: String): String {
        val navn = try {
            val response = valider(client.hentPersonNavn(fnr))
            response.data?.hentPerson?.navn?.get(0)?.let { navn ->
                listOfNotNull(navn.fornavn, navn.mellomnavn, navn.etternavn)
                    .filter { it.isNotBlank() }
                    .joinToString(" ")
            } ?: throw PdlResponseMissingData("Navn mangler i PDL-data")
        } catch (e:Exception) {
            log.error(e) { "Klarte ikke å hente navn" }
            throw e
        }
        return navn
    }

}
