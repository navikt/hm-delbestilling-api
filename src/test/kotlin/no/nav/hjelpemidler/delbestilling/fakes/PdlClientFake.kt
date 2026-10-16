package no.nav.hjelpemidler.delbestilling.fakes

import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.PdlClientInterface
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.PdlPersonResponse
import no.nav.hjelpemidler.delbestilling.testdata.PdlRespons

class PdlClientFake : PdlClientInterface {

    private val default = PdlRespons.person()
    private val responses = mutableMapOf<String, PdlPersonResponse>()

    fun svar(fnr: String, response: PdlPersonResponse) {
        responses[fnr] = response
    }

    private fun responsFor(fnr: String) = responses[fnr] ?: default

    override suspend fun hentKommunenummer(fnummer: String): PdlPersonResponse = responsFor(fnummer)

    override suspend fun hentPersonNavn(fnr: String): PdlPersonResponse = responsFor(fnr)

    override suspend fun hentPersonNavnOgAdresse(fnr: String): PdlPersonResponse = responsFor(fnr)
}
