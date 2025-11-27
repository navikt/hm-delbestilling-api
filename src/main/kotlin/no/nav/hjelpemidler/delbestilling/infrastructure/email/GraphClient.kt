package no.nav.hjelpemidler.delbestilling.infrastructure.email

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.hjelpemidler.delbestilling.config.AppConfig
import no.nav.hjelpemidler.delbestilling.infrastructure.navCorrelationId
import no.nav.hjelpemidler.http.createHttpClient
import no.nav.hjelpemidler.http.openid.OpenIDClient
import no.nav.hjelpemidler.http.openid.bearerAuth


private val log = KotlinLogging.logger { }

fun defaultHttpClient(engine: HttpClientEngine = CIO.create()) = createHttpClient(engine = engine) {
    expectSuccess = true

    install(HttpRequestRetry) {
        retryOnExceptionOrServerErrors(maxRetries = 5)
        exponentialDelay()
    }

    install(Logging) {
        level = LogLevel.BODY
    }

    defaultRequest {
        accept(io.ktor.http.ContentType.Application.Json)
        contentType(io.ktor.http.ContentType.Application.Json)
    }
}

class GraphClient(
    private val openIDClient: OpenIDClient,
    private val client: HttpClient = defaultHttpClient(),
    private val baseUrl: String = "https://graph.microsoft.com/v1.0",
    private val scope: String = "https://graph.microsoft.com/.default",
    private val avsender: String = AppConfig.EPOST_AVSENDER
) : GraphClientInterface {

    override suspend fun sendEmail(recipentEmail: String, subject: String, bodyText: String, contentType: ContentType) {
        val body = SendMailRequest(
            message = Message(
                subject = subject,
                body = ItemBody(contentType, bodyText),
                toRecipients = listOf(Recipient(EmailAddress(recipentEmail)))
            ),
            saveToSentItems = true,
        )

        try {
            withContext(Dispatchers.IO) {
                val tokenSet = openIDClient.grant(scope)
                client.post("$baseUrl/users/$avsender/sendMail") {
                    bearerAuth(tokenSet)
                    navCorrelationId()
                    setBody(body)
                }
            }
        } catch (t: Throwable) {
            log.error(t) { "Sending av epost feilet for epost: to=$recipentEmail, subject=$subject, content=$bodyText, contentType=$contentType" }
            throw t
        }
    }
}