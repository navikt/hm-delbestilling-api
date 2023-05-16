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
import no.nav.tms.token.support.azure.exchange.AzureService
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class PdlClientTest {

    private val azureService = mockk<AzureService>()

    @BeforeEach
    fun setup() {
        every {
            runBlocking {
                azureService.getAccessToken(any())
            }
        } returns "token"
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
            val pdlClient = PdlClient(azureService, mockEngine, "test", "test")
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

