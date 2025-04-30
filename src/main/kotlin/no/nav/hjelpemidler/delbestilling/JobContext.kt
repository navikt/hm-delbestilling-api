package no.nav.hjelpemidler.delbestilling

import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.http.contentType
import no.nav.hjelpemidler.delbestilling.config.isProd
import no.nav.hjelpemidler.http.createHttpClient
import no.nav.hjelpemidler.http.openid.azureADClient
import kotlin.time.Duration.Companion.seconds


class JobContext {
    val azureClient = azureADClient {
        cache(leeway = 10.seconds) {
            maximumSize = 100
        }
    }

    val client = createHttpClient(engine = CIO.create()) {
        expectSuccess = true

        install(HttpRequestRetry) {
            retryOnExceptionOrServerErrors(maxRetries = 5)
            exponentialDelay()
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 60.seconds.inWholeMilliseconds
        }


        install(Logging) {
            level = if (isProd()) LogLevel.INFO else LogLevel.BODY
        }

        defaultRequest {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
        }
    }

}
