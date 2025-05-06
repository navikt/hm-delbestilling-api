package no.nav.hjelpemidler.delbestilling.fakes

import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.PdlClientInterface
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.PdlPersonResponse
import no.nav.hjelpemidler.delbestilling.testdata.PdlRespons

class PdlClientFake : PdlClientInterface {

    var response = PdlRespons.person()

    override suspend fun hentKommunenummer(fnummer: String): PdlPersonResponse {
        return response
    }

    override suspend fun hentPersonNavn(fnr: String): PdlPersonResponse {
        return response
    }
}

