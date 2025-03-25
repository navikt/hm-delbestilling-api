package no.nav.hjelpemidler.delbestilling.pdl

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondBadRequest
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import no.nav.hjelpemidler.delbestilling.infrastructure.monitoring.PersonNotAccessibleInPdl
import no.nav.hjelpemidler.delbestilling.infrastructure.monitoring.PersonNotFoundInPdl
import no.nav.hjelpemidler.http.openid.OpenIDClient
import no.nav.hjelpemidler.http.openid.TokenSet
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class PdlClientTest {

    private val azureAdClient = mockk<OpenIDClient>()

    @BeforeEach
    fun setup() {
        every {
            runBlocking {
                azureAdClient.grant(scope = any())
            }
        } returns TokenSet("", 0, "token")
    }

    @Test
    fun `hent brukers kommunenr`() = runTest {
        val fnr = "123"
        val kommunenr = "3801"

        val mockEngine = MockEngine {
            respondWithBody(pdlRespons(kommunenr))
        }
        val pdlClient = PdlClient(azureAdClient, mockEngine, "test", "test")
        assertEquals(kommunenr, pdlClient.hentKommunenummer(fnr))
    }

    @Test
    fun `bruker ikke finnes i PDL`() = runTest {
        val fnr = "123"

        assertThrows<PersonNotFoundInPdl> {
            val mockEngine = MockEngine {
                respondWithBody(pdlFeilRespons())
            }
            val pdlClient = PdlClient(azureAdClient, mockEngine, "test", "test")
            pdlClient.hentKommunenummer(fnr)
        }
    }

    @Test
    fun `bruker har gradering STRENGT_FORTROLIG (kode 6) `() = runTest {
        val fnr = "123"
        val kommunenr = "3801"

        println(pdlKode6Respons(kommunenr))

        assertThrows<PersonNotAccessibleInPdl> {
            val mockEngine = MockEngine {
                respondWithBody(pdlKode6Respons(kommunenr))
            }
            val pdlClient = PdlClient(azureAdClient, mockEngine, "test", "test")
            pdlClient.hentKommunenummer(fnr)
        }
    }

    @Test
    fun `bruker har gradering fortrolig (kode 7) `() = runTest {
        val fnr = "123"
        val kommunenr = "3801"

        println(pdlKode6Respons(kommunenr))

        assertThrows<PersonNotAccessibleInPdl> {
            val mockEngine = MockEngine {
                respondWithBody(pdlKode7Respons(kommunenr))
            }
            val pdlClient = PdlClient(azureAdClient, mockEngine, "test", "test")
            pdlClient.hentKommunenummer(fnr)
        }
    }

    @Test
    fun `default ktor exception ved feilmelding fra klient`() = runTest {
        val fnr = "123"

        assertThrows<ResponseException> {
            val mockEngine = MockEngine { _ ->
                respondBadRequest()
            }
            val pdlClient = PdlClient(azureAdClient, mockEngine, "test", "test")
            pdlClient.hentKommunenummer(fnr)
        }
    }

    @Test
    fun `kan ignorere adressebeskyttelse på navneoppslag`() = runTest {
        val fnr = "123"
        val mockEngine = MockEngine { respondWithBody(pdlNavnRespons("fornavn", "etternavn")) }
        val pdlClient = PdlClient(azureAdClient, mockEngine, "test", "test")
        val response = pdlClient.hentPersonNavn(fnr, validerAdressebeskyttelse = false)
        assertEquals("fornavn", response.data!!.hentPerson!!.navn[0].fornavn)
    }
}

@Language("JSON")
fun pdlRespons(kommunenr: String) =
    """
{
  "data": {
    "hentPerson": {
      "bostedsadresse": [
        {
          "vegadresse": {
            "kommunenummer": "$kommunenr"
          }
        }
      ]
    }
  }
}
    """.trimIndent()

@Language("JSON")
fun pdlFeilRespons() =
    """
{
  "errors": [
  {
      "message": "Fant ikke person",
      "locations": [
        {
          "line": 2,
          "column": 5
        }
      ],
      "path": [
        "hentPerson"
      ],
      "extensions": {
        "code": "not_found",
        "classification": "ExecutionAborted"
      }
    }
  ],
  "data": {
    "hentPerson": null
  }
}
    """.trimIndent()

@Language("JSON")
fun pdlKode6Respons(kommunenr: String) =
    """
{
  "data": {
    "hentPerson": {
      "bostedsadresse": [
        {
          "vegadresse": {
            "kommunenummer": "$kommunenr"
          }
        }
      ],
      "adressebeskyttelse": [
        {
          "gradering": "${Gradering.STRENGT_FORTROLIG}"
        }
      ]
    }
  }
}
    """.trimIndent()

@Language("JSON")
fun pdlKode7Respons(kommunenr: String) =
    """
{
  "data": {
    "hentPerson": {
      "bostedsadresse": [
        {
          "vegadresse": {
            "kommunenummer": "$kommunenr"
          }
        }
      ],
      "adressebeskyttelse": [
        {
          "gradering": "${Gradering.FORTROLIG}"
        }
      ]
    }
  }
}
    """.trimIndent()

@Language("JSON")
fun pdlNavnRespons(fornavn: String, etternavn: String) =
    """
{
  "data": {
    "hentPerson": {
      "navn": [
        {
          "fornavn": "$fornavn",
          "etternavn": "$etternavn"
        }
      ],
      "adressebeskyttelse": [
        {
          "gradering": "${Gradering.FORTROLIG}"
        }
      ]
    }
  }
}
    """.trimIndent()

private fun MockRequestHandleScope.respondWithBody(body: String): HttpResponseData =
    respond(
        content = ByteReadChannel(body),
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, "application/json")
    )
