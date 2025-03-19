package no.nav.hjelpemidler.delbestilling.grunndata

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.headers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.hjelpemidler.delbestilling.Config
import no.nav.hjelpemidler.delbestilling.grunndata.requests.alleDelerSomKanBestillesRequest
import no.nav.hjelpemidler.delbestilling.grunndata.requests.alleHjmMedIdEllerSeriesIdRequest
import no.nav.hjelpemidler.delbestilling.grunndata.requests.hmsArtNrRequest
import no.nav.hjelpemidler.delbestilling.grunndata.requests.compatibleWithRequest
import no.nav.hjelpemidler.delbestilling.navCorrelationId
import no.nav.hjelpemidler.http.createHttpClient
import java.util.UUID

private val logger = KotlinLogging.logger { }

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
        defaultRequest {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun hentHjelpemiddel(hmsnr: String): ProduktResponse {
        logger.info { "Henter hjelpemiddel $hmsnr fra grunndata" }
        return try {
            withContext(Dispatchers.IO) {
                client.post(searchUrl) {
                    headers {
                        navCorrelationId()
                    }
                    setBody(hmsArtNrRequest(hmsnr))
                }.body()
            }
        } catch (e: Exception) {
            logger.error(e) { "Henting av hjelpemiddel fra grunndata feilet" }
            throw e
        }
    }

    suspend fun hentDeler(seriesId: UUID, produktId: UUID): ProduktResponse {
        logger.info { "Henter deler for seriesId $seriesId og produktId $produktId fra grunndata" }
        return try {
            withContext(Dispatchers.IO) {
                client.post(searchUrl) {
                    headers {
                        navCorrelationId()
                    }
                    setBody(compatibleWithRequest(seriesId, produktId))
                }.body()
            }
        } catch (e: Exception) {
            logger.error(e) { "Henting av deler fra grunndata feilet" }
            throw e
        }
    }

    suspend fun hentAlleDelerSomKanBestilles(): ProduktResponse {
        logger.info { "Henter alle deler som kan bestilles fra grunndata" }
        return try {
            withContext(Dispatchers.IO) {
                client.post(searchUrl) {
                    headers {
                        navCorrelationId()
                    }
                    setBody(alleDelerSomKanBestillesRequest())
                }.body()
            }
        } catch (e: Exception) {
            logger.error(e) { "Henting av deler fra grunndata feilet" }
            throw e
        }
    }

    suspend fun hentAlleHjmMedIdEllerSeriesId(seriesIds: Set<UUID>, produktIds: Set<UUID>): ProduktResponse {
        logger.info { "Henter alle hjm med gitte id eller seriesId fra grunndata" }
        return try {
            withContext(Dispatchers.IO) {
                client.post(searchUrl) {
                    headers {
                        navCorrelationId()
                    }
                    setBody(alleHjmMedIdEllerSeriesIdRequest(seriesIds = seriesIds, produktIds = produktIds))
                }.body()
            }
        } catch (e: Exception) {
            logger.error(e) { "Henting av hjm fra grunndata feilet" }
            throw e
        }
    }
}