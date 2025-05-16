package no.nav.hjelpemidler.delbestilling.infrastructure.email

import com.azure.identity.ClientSecretCredentialBuilder
import com.microsoft.graph.models.BodyType
import com.microsoft.graph.models.EmailAddress
import com.microsoft.graph.models.ItemBody
import com.microsoft.graph.models.Message
import com.microsoft.graph.models.Recipient
import com.microsoft.graph.serviceclient.GraphServiceClient
import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.config.AppConfig
import no.nav.hjelpemidler.delbestilling.config.isDev
import com.microsoft.graph.core.authentication.AzureIdentityAuthenticationProvider
import com.microsoft.graph.core.requests.GraphClientFactory
import com.microsoft.graph.users.item.sendmail.SendMailPostRequestBody
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

private val log = KotlinLogging.logger {}

class Email {
    private val avsender = AppConfig.EPOST_AVSENDER

    private val credential =
        ClientSecretCredentialBuilder()
            .clientId(AppConfig.AZURE_APP_CLIENT_ID)
            .tenantId(AppConfig.AZURE_APP_TENANT_ID)
            .clientSecret(AppConfig.AZURE_APP_CLIENT_SECRET)
            .build()
    private val allowedHosts = arrayOf("graph.microsoft.com")
    private val scope = "https://graph.microsoft.com/.default"
    private val authProvider = AzureIdentityAuthenticationProvider(credential, allowedHosts, scope)

    val graphClient = GraphServiceClient(
        authProvider,
        GraphClientFactory
            .create()
            .connectTimeout(20.seconds.toJavaDuration())
            .readTimeout(60.seconds.toJavaDuration())
            .writeTimeout(30.seconds.toJavaDuration())
            .build()
    )

    fun sendSimpleMessage(
        to: String,
        subject: String,
        contentType: BodyType,
        content: String,
    ) {
        val message = Message()

        message.toRecipients = buildList {
            add(Recipient().apply {
                emailAddress = EmailAddress().apply {
                    address = to
                }
            })
        }

        message.subject = when (isDev()) {
            true -> "[TEST] $subject"
            else -> subject
        }

        val body = ItemBody()
        body.contentType = contentType
        body.content = content
        message.body = body

        log.info {
            """
            E-post til avsending:
            To: $to
            Subject: ${message.subject}
            ContentType: ${message.body!!.contentType}
            Body: ${message.body!!.content}
            Avsender: $avsender
        """.trimIndent()
        }

        val mailPostRequest = SendMailPostRequestBody().apply {
            this.message = message
            saveToSentItems = true
        }

        if (isDev()) {
            log.info { "Ignorerer utsending av epost i dev." }
            return
        }

        try {
            graphClient
                .users().byUserId(avsender)
                .sendMail().post(mailPostRequest)
            log.info { "Mail to $to sent" }
        } catch (e: Exception) {
            log.error(e) { "Got error sending mail: to=$to, subject=$subject, contentType=$contentType, content=$content" }
            throw e
        }
    }

    fun sendTestMail(
        to: String = "digitalisering.av.hjelpemidler.og.tilrettelegging@nav.no",
        subject: String = "[TEST] hm-delbestilling-api",
        contentType: BodyType = BodyType.Text,
        content: String = "Dette er bare en test av epostutsending fra hm-delbestilling-api. Vennligst ignorer meg.",
    ) {
        val message = Message()

        message.toRecipients = buildList {
            add(Recipient().apply {
                emailAddress = EmailAddress().apply {
                    address = to
                }
            })
        }

        message.subject = subject

        val body = ItemBody()
        body.contentType = contentType
        body.content = content
        message.body = body

        val mailPostRequest = SendMailPostRequestBody().apply {
            this.message = message
            saveToSentItems = true
        }

        try {
            graphClient
                .users().byUserId(avsender)
                .sendMail().post(mailPostRequest)
            log.info { "Mail to $to sent" }
        } catch (e: Exception) {
            log.error(e) { "Got error sending mail: to=$to, subject=$subject, contentType=$contentType, content=$content" }
            throw e
        }
    }
}