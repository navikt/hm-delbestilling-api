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
import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.Config
import no.nav.hjelpemidler.delbestilling.navCorrelationId
import no.nav.hjelpemidler.http.createHttpClient

private val log = KotlinLogging.logger { }

class OppslagClient(
    engine: HttpClientEngine = CIO.create(),
    private val url: String = Config.OPPSLAG_API_URL,
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
                client.get("$url/api/geografi/kommuner/$kommunenr") {
                    headers {
                        navCorrelationId()
                    }
                }.body()
            }
        } catch (e: Exception) {
            log.warn(e) { "Klarte ikke å hente kommune for kommunenr: $kommunenr" }
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
