package no.nav.hjelpemidler.hjelpemidlerdigitalSoknadapi.tjenester.norg

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.hjelpemidler.delbestilling.Config
import no.nav.hjelpemidler.delbestilling.isProd
import no.nav.hjelpemidler.delbestilling.navCorrelationId
import no.nav.hjelpemidler.http.createHttpClient

private val log = KotlinLogging.logger {}

class NorgClient(
    engine: HttpClientEngine = CIO.create(),
    private val baseUrl: String = Config.NORG_API_URL,
) {

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

    internal suspend fun hentArbeidsfordelingenheter(kommunenummer: String): List<ArbeidsfordelingEnhet> {
        val url = "$baseUrl/arbeidsfordeling/enheter/bestmatch"
        log.info { "Henter arbeidsfordelingenhet med url: '$url'" }

        return withContext(Dispatchers.IO) {
            client.post(url) {
                navCorrelationId()
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
                setBody(
                    mapOf(
                        "geografiskOmraade" to kommunenummer,
                        "tema" to "HJE",
                        "temagruppe" to "HJLPM",
                    ),
                )
            }.body()
        }
    }
}
