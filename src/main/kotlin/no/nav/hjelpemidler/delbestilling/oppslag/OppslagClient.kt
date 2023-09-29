package no.nav.hjelpemidler.delbestilling.oppslag

import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import no.nav.hjelpemidler.delbestilling.Config
import no.nav.hjelpemidler.delbestilling.navCorrelationId
import no.nav.hjelpemidler.http.createHttpClient
import no.nav.hjelpemidler.http.openid.OpenIDClient
import no.nav.hjelpemidler.http.openid.bearerAuth

private val log = KotlinLogging.logger { }

class OppslagClient(
    private val azureAdClient: OpenIDClient,
    engine: HttpClientEngine = CIO.create(),
    private val url: String = Config.OPPSLAG_API_URL,
    private val apiScope: String = Config.OPPSLAG_API_SCOPE,
) {
    private val client = createHttpClient(engine = engine) {
        expectSuccess = true
        defaultRequest {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun hentKommune(kommunenr: String): KommuneDto {
        return try {
            withContext(Dispatchers.IO) {
                val tokenSet = azureAdClient.grant(apiScope)
                client.get("$url/api/geografi/kommuner/$kommunenr") {
                    bearerAuth(tokenSet)
                    headers {
                        navCorrelationId()
                    }
                }.body()
            }
        } catch (e: Exception) {
            log.warn(e) { "Klarte ikke å hente brukers kommune" }
            throw e
        }
    }
}

data class KommuneDto(
    val fylkesnummer: String,
    val fylkesnavn: String,
    val kommunenummer: String,
    val kommunenavn: String,
)