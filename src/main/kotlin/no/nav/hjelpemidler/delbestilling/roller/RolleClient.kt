package no.nav.hjelpemidler.delbestilling.roller

import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import no.nav.hjelpemidler.delbestilling.Config
import no.nav.hjelpemidler.delbestilling.navCorrelationId
import no.nav.hjelpemidler.http.createHttpClient
import no.nav.tms.token.support.tokendings.exchange.TokendingsService

private val logger = KotlinLogging.logger { }

class RolleClient(
    private val tokendingsService: TokendingsService,
    engine: HttpClientEngine = CIO.create(),
    private val url: String = Config.ROLLER_API_URL,
    private val scope: String = Config.ROLLER_API_SCOPE
) {

    private val client = createHttpClient(engine = engine) {
        expectSuccess = true
        install(HttpRequestRetry) {
            retryOnExceptionOrServerErrors(maxRetries = 5)
            exponentialDelay()
        }
        defaultRequest {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun hentDelbestillerRolle(token: String): DelbestillerResponse {
        val exchangedToken = tokendingsService.exchangeToken(token, scope)

        return try {
            withContext(Dispatchers.IO) {
                client.get("$url/api/delbestiller") {
                    headers {
                        header("Authorization", "Bearer $exchangedToken")
                        navCorrelationId()
                    }
                }.body()
            }
        } catch (e: Exception) {
            logger.error(e) { "Henting av delbestillerrolle feilet" }
            throw e
        }
    }
}

data class Delbestiller(
    val kanBestilleDeler: Boolean,
    val harXKLager: Boolean,
    val kommunaleOrgs: List<Organisasjon>?,
    val erKommunaltAnsatt: Boolean,
    val erIPilot: Boolean,
)

data class DelbestillerResponse(
    val delbestillerrolle: Delbestiller
)
