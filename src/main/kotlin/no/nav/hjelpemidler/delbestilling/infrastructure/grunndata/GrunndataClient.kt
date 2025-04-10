package no.nav.hjelpemidler.delbestilling.infrastructure.grunndata

import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.hjelpemidler.delbestilling.Config
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.queries.alleDelerSomKanBestillesQuery
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.queries.alleHjelpemiddelMedIdEllerSeriesIdQuery
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.queries.compatibleWithQuery
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.queries.hmsArtNrQuery
import no.nav.hjelpemidler.delbestilling.isProd
import no.nav.hjelpemidler.delbestilling.navCorrelationId
import no.nav.hjelpemidler.http.createHttpClient
import java.util.UUID

class GrunndataClient(
    engine: HttpClientEngine = CIO.create(),
    baseUrl: String = Config.GRUNNDATA_API_URL,
) {
    private val searchUrl = "$baseUrl/products/_search"

    private val client = createHttpClient(engine = engine) {
        expectSuccess = true
        install(HttpRequestRetry) {
            retryOnExceptionOrServerErrors(maxRetries = 5)
            exponentialDelay()
        }
        install(Logging) {
            level = if (isProd()) LogLevel.INFO else LogLevel.BODY
        }
        defaultRequest {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
        }
    }

    private suspend fun executeQuery(query: JsonNode): ProduktResponse {
        return withContext(Dispatchers.IO) {
            client.post(searchUrl) {
                headers {
                    navCorrelationId()
                }
                setBody(query)
            }.body()
        }
    }

    suspend fun hentProdukt(hmsnr: String): ProduktResponse {
        return executeQuery(hmsArtNrQuery(hmsnr))
    }

    suspend fun hentDeler(seriesId: UUID, produktId: UUID): ProduktResponse {
        return executeQuery(compatibleWithQuery(seriesId, produktId))
    }

    suspend fun hentAlleDelerSomKanBestilles(): ProduktResponse {
        return executeQuery(alleDelerSomKanBestillesQuery())
    }

    suspend fun hentAlleHjmMedIdEllerSeriesId(seriesIds: Set<UUID>, produktIds: Set<UUID>): ProduktResponse {
        return executeQuery(alleHjelpemiddelMedIdEllerSeriesIdQuery(seriesIds = seriesIds, produktIds = produktIds))
    }
}