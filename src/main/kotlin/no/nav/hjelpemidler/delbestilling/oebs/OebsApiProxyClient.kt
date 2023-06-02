package no.nav.hjelpemidler.delbestilling.oebs

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.headers
import mu.KotlinLogging
import no.nav.hjelpemidler.delbestilling.Config
import no.nav.hjelpemidler.http.createHttpClient
import io.ktor.serialization.jackson.jackson
import no.nav.tms.token.support.azure.exchange.AzureService

private val logg = KotlinLogging.logger {}

class OebsApiProxyClient(
    private val azureAdService: AzureService,
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

        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.BODY
        }

        defaultRequest {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun hentUtlånPåArtnrOgSerienr(artnr: String, serienr: String): Utlån? {
        try {
            logg.info { "apiScope: $apiScope" }
            logg.info { "baseUrl: $baseUrl" }
            val token = azureAdService.getAccessToken(apiScope)
            val url = "$baseUrl/utlanSerienrArtnr"
            logg.info { "Gjør request mot $url" }
            val httpResponse = client.request( url) {
                method = HttpMethod.Post
                headers {
                    header("Authorization", "Bearer $token")
                    setBody(UtlånPåArtnrOgSerienrRequest(artnr, serienr))
                }
            }
            val body = httpResponse.body<Unit>()
            logg.info { "body: $body" }
            val response: UtlånPåArtnrOgSerienrResponse = httpResponse.body()
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