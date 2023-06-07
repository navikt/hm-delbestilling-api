package no.nav.hjelpemidler.delbestilling.pdl

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondBadRequest
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.hjelpemidler.delbestilling.exceptions.PersonNotAccessibleInPdl
import no.nav.hjelpemidler.delbestilling.exceptions.PersonNotFoundInPdl
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
        } returns TokenSet("", 0, "token",)
    }

    @Test
    fun `hent brukers kommunenr`() {
        val fnr = "123"
        val kommunenr = "3801"

        runBlocking {
            val mockEngine = MockEngine { _ ->
                respond(
                    content = ByteReadChannel(pdlRespons(kommunenr)),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
            val pdlClient = PdlClient(azureAdClient, mockEngine, "test", "test")
            assertEquals(kommunenr, pdlClient.hentKommunenummer(fnr))
        }
    }

    @Test
    fun `bruker ikke finnes i PDL`() {
        val fnr = "123"

        assertThrows<PersonNotFoundInPdl> {
            runBlocking {
                val mockEngine = MockEngine { _ ->
                    respond(
                        content = ByteReadChannel(pdlFeilRespons()),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                val pdlClient = PdlClient(azureAdClient, mockEngine, "test", "test")
                pdlClient.hentKommunenummer(fnr)
            }
        }
    }

    @Test
    fun `bruker har gradering STRENGT_FORTROLIG (kode 6) `() {
        val fnr = "123"
        val kommunenr = "3801"

        println(pdlKode6Respons(kommunenr))

        assertThrows<PersonNotAccessibleInPdl> {
            runBlocking {
                val mockEngine = MockEngine { _ ->
                    respond(
                        content = ByteReadChannel(pdlKode6Respons(kommunenr)),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                val pdlClient = PdlClient(azureAdClient, mockEngine, "test", "test")
                pdlClient.hentKommunenummer(fnr)
            }
        }
    }

    @Test
    fun `bruker har gradering fortrolig (kode 7) `() {
        val fnr = "123"
        val kommunenr = "3801"

        println(pdlKode6Respons(kommunenr))

        assertThrows<PersonNotAccessibleInPdl> {
            runBlocking {
                val mockEngine = MockEngine { _ ->
                    respond(
                        content = ByteReadChannel(pdlKode7Respons(kommunenr)),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                val pdlClient = PdlClient(azureAdClient, mockEngine, "test", "test")
                pdlClient.hentKommunenummer(fnr)
            }
        }
    }

    @Test
    fun `default ktor exception ved feilmelding fra klient`() {
        val fnr = "123"

        assertThrows<ResponseException> {
            runBlocking {
                val mockEngine = MockEngine { _ ->
                    respondBadRequest()
                }
                val pdlClient = PdlClient(azureAdClient, mockEngine, "test", "test")
                pdlClient.hentKommunenummer(fnr)
            }
        }
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

