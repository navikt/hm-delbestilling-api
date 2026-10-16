package no.nav.hjelpemidler.delbestilling.infrastructure.pdl

interface PdlClientInterface {
    suspend fun hentKommunenummer(fnummer: String): PdlPersonResponse
    suspend fun hentPersonNavn(fnr: String): PdlPersonResponse
}