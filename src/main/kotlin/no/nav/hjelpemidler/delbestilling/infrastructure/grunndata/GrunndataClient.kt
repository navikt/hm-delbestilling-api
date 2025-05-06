package no.nav.hjelpemidler.delbestilling.infrastructure.grunndata

import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.hjelpemidler.delbestilling.config.AppConfig
import no.nav.hjelpemidler.delbestilling.infrastructure.defaultHttpClient
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.queries.alleDelerSomKanBestillesQuery
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.queries.alleHjelpemiddelMedIdEllerSeriesIdQuery
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.queries.compatibleWithQuery
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.queries.hmsArtNrQuery
import no.nav.hjelpemidler.delbestilling.infrastructure.navCorrelationId
import java.util.UUID

class GrunndataClient(
    private val client: HttpClient = defaultHttpClient(),
    baseUrl: String = AppConfig.GRUNNDATA_API_URL,
) {

    private val searchUrl = "$baseUrl/products/_search"

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