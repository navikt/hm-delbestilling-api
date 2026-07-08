package no.nav.hjelpemidler.delbestilling.infrastructure.pdl

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.delbestilling.PersonNavnOgAdresse
import no.nav.hjelpemidler.delbestilling.infrastructure.geografi.Geografioppslag
import no.nav.hjelpemidler.domain.geografi.Veiadresse
import no.nav.hjelpemidler.domain.person.Personnavn

private val log = KotlinLogging.logger {}

class Pdl(private val client: PdlClientInterface, private val geografioppslag: Geografioppslag) {

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

    suspend fun hentNavn(fnr: String): String {
        val navn = try {
            val response = valider(client.hentPersonNavn(fnr))
            response.data?.hentPerson?.navn?.get(0)?.let { navn ->
                "${navn.fornavn} ${navn.mellomnavn ?: ""} ${navn.etternavn}".trim()
            } ?: throw PdlResponseMissingData("Navn mangler i PDL-data")
        } catch (e:Exception) {
            log.error(e) { "Klarte ikke å hente navn" }
            throw e
        }
        return navn
    }


    suspend fun henthentPersonNavnOgAdresse(fnr: String): PersonNavnOgAdresse {
        val response = try {
            valider(client.hentPersonNavnOgAdresse(fnr))
        } catch (e:Exception) {
            log.error(e) { "Klarte ikke å hente Navn og Adresse" }
            throw e
        }

        val navn = response.data?.hentPerson?.navn?.get(0) ?: throw PdlResponseMissingData("Navn mangler i PDL-data")
        val adresse = response.data.hentPerson.bostedsadresse[0].vegadresse ?: throw PdlResponseMissingData("Adresse mangler i PDL-data")
        val postnummer = adresse.postnummer ?: throw PdlResponseMissingData("Postnummer mangler i PDL-data")
        val poststed = geografioppslag.hentPoststed(postnummer)

        return PersonNavnOgAdresse(
            navn = Personnavn(
                fornavn = navn.fornavn,
                mellomnavn = navn.mellomnavn,
                etternavn = navn.etternavn
            ),
            adresse = Veiadresse(
                adresse = "${adresse.adressenavn ?: ""} ${adresse.husnummer ?: ""}${adresse.husbokstav ?: ""}".trim(),
                postnummer = postnummer,
                poststed = poststed,
            )
        )
    }
}
