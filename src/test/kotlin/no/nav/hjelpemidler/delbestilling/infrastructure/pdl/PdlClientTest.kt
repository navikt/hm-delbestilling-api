package no.nav.hjelpemidler.delbestilling.infrastructure.pdl

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondBadRequest
import io.ktor.client.plugins.ResponseException
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import no.nav.hjelpemidler.delbestilling.infrastructure.defaultHttpClient
import no.nav.hjelpemidler.http.openid.OpenIDClient
import no.nav.hjelpemidler.http.openid.TokenSet
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class PdlClientTest {

    private val fnr = "12345678910"
    private val kommunenr = "3801"

    private val azureAd = mockk<OpenIDClient>().apply {
        coEvery { grant(scope = any()) } returns TokenSet("token", 3600, "Bearer")
    }

    private fun pdlClientWithResponse(mockResponse: String) =
        PdlClient(azureAd, mockClient(mockResponse), "url", "scope")


    @Test
    fun `happy path`() = runTest {
        val pdlClient = pdlClientWithResponse(pdlRespons(kommunenr))
        assertEquals(
            kommunenr,
            pdlClient.hentKommunenummer(fnr).data?.hentPerson?.bostedsadresse?.first()?.vegadresse?.kommunenummer
        )
    }

    @Test
    fun `skal kaste default ktor exception ved feilmelding fra klient`() = runTest {
        val pdlClient = PdlClient(azureAd, defaultHttpClient(MockEngine {
            respondBadRequest()
        }), "url", "scope")

        assertThrows<ResponseException> { pdlClient.hentKommunenummer(fnr) }
    }

}