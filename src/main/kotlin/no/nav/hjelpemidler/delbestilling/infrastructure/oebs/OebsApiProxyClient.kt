package no.nav.hjelpemidler.delbestilling.infrastructure.oebs

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
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
import no.nav.hjelpemidler.delbestilling.Config
import no.nav.hjelpemidler.delbestilling.isProd
import no.nav.hjelpemidler.delbestilling.navCorrelationId
import no.nav.hjelpemidler.http.createHttpClient
import no.nav.hjelpemidler.http.openid.OpenIDClient
import no.nav.hjelpemidler.http.openid.bearerAuth

private val log = KotlinLogging.logger {}

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

        install(Logging) {
            level = if (isProd()) LogLevel.INFO else LogLevel.BODY
        }

        defaultRequest {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
        }
    }

    private suspend inline fun <reified T> executeRequest(url: String, method: HttpMethod, body: Any? = null): T {
        try {
            return withContext(Dispatchers.IO) {
                val tokenSet = azureAdClient.grant(apiScope)
                val httpResponse = client.request(url) {
                    this.method = method
                    bearerAuth(tokenSet)
                    navCorrelationId()
                    if (body != null) {
                        setBody(body)
                    }
                }
                httpResponse.body()
            }
        } catch (t: Throwable) {
            log.error(t) { "OeBS request feilet: [$method] $url" }
            throw t
        }
    }

    private suspend inline fun <reified T> post(url: String, body: Any? = null): T =
        executeRequest(url, HttpMethod.Post, body)

    suspend fun hentUtlånPåArtnrOgSerienr(artnr: String, serienr: String): UtlånPåArtnrOgSerienrResponse =
        post("$baseUrl/utlanSerienrArtnr", UtlånPåArtnrOgSerienrRequest(artnr, serienr))

    suspend fun hentFnrSomHarUtlånPåArtnr(artnr: String): List<Utlån> =
        post("$baseUrl/utlanArtnr", artnr)

    suspend fun hentPersoninfo(fnr: String): List<OebsPersoninfo> =
        post("$baseUrl/getLeveringsaddresse", fnr)

    suspend fun hentBrukerpassinfo(fnr: String): Brukerpass =
        post("$baseUrl/hent-brukerpass", FnrDto(fnr))

    suspend fun hentLagerstatusForKommunenummer(kommunenummer: String, hmsnrs: List<String>): List<LagerstatusResponse> =
        post("$baseUrl/lager/sentral/$kommunenummer", LagerstatusRequest(hmsnrs))

    suspend fun hentLagerstatusForEnhetnr(enhetnr: String, hmsnrs: List<String>): List<LagerstatusResponse> =
        post("$baseUrl/lager/sentral/enhet/$enhetnr", LagerstatusRequest(hmsnrs))
}


