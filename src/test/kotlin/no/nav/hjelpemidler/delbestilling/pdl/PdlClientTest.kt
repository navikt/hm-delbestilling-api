package no.nav.hjelpemidler.delbestilling.pdl

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.hjelpemidler.http.openid.OpenIDClient
import no.nav.hjelpemidler.http.openid.TokenSet
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class PdlClientTest {

    private val azureAdClient = mockk<OpenIDClient>()

    @BeforeEach
    fun setup() {
        every {
            runBlocking {
                azureAdClient.grant(scope = any())
            }
        } returns TokenSet("", 0, "token", )
    }

    @Test
    fun `hent brukers kommunenr`() {
        val fnr = "123"

        runBlocking {
            val mockEngine = MockEngine { _ ->
                respond(
                    content = ByteReadChannel(pdlRespons()),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
            val pdlClient = PdlClient(azureAdClient, mockEngine, "test", "test")
            pdlClient.hentKommunenummer(fnr)
        }
    }
}

@Language("JSON")
fun pdlRespons() =
    """
{
  "data": {
    "hentPerson": {
      "bostedsadresse": [
        {
          "vegadresse": {
            "kommunenummer": "3801"
          }
        }
      ]
    }
  }
}
""".trimIndent()

