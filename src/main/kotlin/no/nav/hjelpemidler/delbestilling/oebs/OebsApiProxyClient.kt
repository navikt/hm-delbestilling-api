package no.nav.hjelpemidler.delbestilling.oebs

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.headers
import mu.KotlinLogging
import no.nav.hjelpemidler.delbestilling.Config
import no.nav.hjelpemidler.http.createHttpClient
import no.nav.tms.token.support.azure.exchange.AzureService
import java.util.UUID

private val logg = KotlinLogging.logger {}

class OebsApiProxyClient(
    private val azureAdService: AzureService,
    engine: HttpClientEngine = CIO.create(),
    private val baseUrl: String = Config.OEBS_API_URL,
    private val apiScope: String = Config.OEBS_API_SCOPE,
) {
    private val client = createHttpClient(engine = engine) {
        expectSuccess = false
        install(HttpRequestRetry) {
            retryOnExceptionOrServerErrors(maxRetries = 5)
            exponentialDelay()
        }
        defaultRequest {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun hentUtlånPåArtnrOgSerienr(artnr: String, serienr: String): Utlån? {
        try {
            val token = azureAdService.getAccessToken(apiScope)
            val httpResponse = client.request(baseUrl + "/utlanSerienrArtnr") {
                method = HttpMethod.Post
                headers {
                    contentType(ContentType.Application.Json)
                    header("Authorization", "Bearer $token")
                    header("Content-Type", "application/json")
                    setBody(UtlånPåArtnrOgSerienrRequest(artnr, serienr))
                }
            }
            val response = httpResponse.body<UtlånPåArtnrOgSerienrResponse>()
            return response.utlån
        } catch (e: Exception) {
            logg.error(e) { "Klarte ikke hente utlån på artnr og serienr" }
            throw e
        }
    }
}

data class UtlånPåArtnrOgSerienrRequest(
    val artnr: String,
    val serienr: String
)

data class UtlånPåArtnrOgSerienrResponse(
    val utlån: Utlån?
)


data class Utlån(
    val fnr: String,
    val artnr: String,
    val serienr: String,
    val utlånsDato: String,
)