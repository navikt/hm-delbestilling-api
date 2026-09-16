package no.nav.hjelpemidler.delbestilling.infrastructure.email

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.hjelpemidler.delbestilling.config.AppConfig
import no.nav.hjelpemidler.delbestilling.infrastructure.defaultHttpClient
import no.nav.hjelpemidler.delbestilling.infrastructure.navCorrelationId
import no.nav.hjelpemidler.http.openid.TexasClient


private val log = KotlinLogging.logger { }

class GraphClient(
    private val texasClient: TexasClient,
    private val scope: String = "https://graph.microsoft.com/.default",
    private val client: HttpClient = defaultHttpClient(tokenSetProvider = texasClient.entraIdApplication(scope)),
    private val baseUrl: String = "https://graph.microsoft.com/v1.0",
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
                client.post("$baseUrl/users/$avsender/sendMail") {
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