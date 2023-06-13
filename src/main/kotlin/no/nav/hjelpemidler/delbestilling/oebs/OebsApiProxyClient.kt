package no.nav.hjelpemidler.delbestilling.oebs

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.serialization.jackson.jackson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import no.nav.hjelpemidler.delbestilling.Config
import no.nav.hjelpemidler.http.createHttpClient
import no.nav.hjelpemidler.http.openid.OpenIDClient
import no.nav.hjelpemidler.http.openid.bearerAuth

private val logg = KotlinLogging.logger {}

class OebsApiProxyClient(
    private val azureAdClient: OpenIDClient,
    engine: HttpClientEngine = CIO.create(),
    private val baseUrl: String = Config.OEBS_API_URL,
    private val apiScope: String = Config.OEBS_API_SCOPE,
) {
    private val client = createHttpClient(engine = engine) {
        expectSuccess = true

        install(ContentNegotiation) {
            jackson {
                registerModule(JavaTimeModule())
                disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            }
        }

        defaultRequest {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun hentUtlånPåArtnrOgSerienr(artnr: String, serienr: String): Utlån? {
        return withContext(Dispatchers.IO) {
            try {
                val tokenSet = azureAdClient.grant(apiScope)
                val httpResponse = client.request("$baseUrl/utlanSerienrArtnr") {
                    method = HttpMethod.Post
                    bearerAuth(tokenSet)
                    setBody(UtlånPåArtnrOgSerienrRequest(artnr, serienr))
                }
                val response = httpResponse.body<UtlånPåArtnrOgSerienrResponse>()

                response.utlån
            } catch (e: Exception) {
                logg.error(e) { "Klarte ikke hente utlån på artnr og serienr" }
                throw e
            }
        }

    }

    suspend fun hentPersoninfo(fnr: String): OebsPersoninfo {
        return withContext(Dispatchers.IO) {
            try {
                val tokenSet = azureAdClient.grant(apiScope)
                val httpResponse = client.request("$baseUrl/getLeveringsaddresse") {
                    method = HttpMethod.Post
                    bearerAuth(tokenSet)
                    setBody(fnr)
                }
                httpResponse.body()
            } catch (e: Exception) {
                logg.error(e) { "Klarte ikke hente leveringsadresse fra OEBS" }
                throw e
            }
        }
    }
}
