package no.nav.hjelpemidler.delbestilling.infrastructure.email

import com.azure.identity.ClientSecretCredentialBuilder
import com.microsoft.graph.authentication.TokenCredentialAuthProvider
import com.microsoft.graph.models.BodyType
import com.microsoft.graph.models.EmailAddress
import com.microsoft.graph.models.ItemBody
import com.microsoft.graph.models.Message
import com.microsoft.graph.models.Recipient
import com.microsoft.graph.models.UserSendMailParameterSet
import com.microsoft.graph.requests.GraphServiceClient
import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.Config
import java.util.LinkedList

private val log = KotlinLogging.logger {}

class Email() {
    private val scopes = listOf("https://graph.microsoft.com/.default")
    private val avsender = "digitalisering.av.hjelpemidler.og.tilrettelegging@nav.no"

    private val credential =
        ClientSecretCredentialBuilder()
            .clientId(Config.AZURE_APP_CLIENT_ID)
            .tenantId(Config.AZURE_APP_TENANT_ID)
            .clientSecret(Config.AZURE_APP_CLIENT_SECRET)
            .build()

    private val authProvider =
        TokenCredentialAuthProvider(
            scopes,
            credential,
        )

    private val graphClient: GraphServiceClient<okhttp3.Request> =
        GraphServiceClient.builder()
            .authenticationProvider(authProvider).buildClient()

    fun sendSimpleMessage(
        to: String,
        subject: String,
        contentType: BodyType,
        content: String,
    ) {
        val message = Message()

        val toRecipientsList: LinkedList<Recipient> = LinkedList<Recipient>()
        val toRecipients = Recipient()
        val emailAddress = EmailAddress()
        emailAddress.address = to
        toRecipients.emailAddress = emailAddress
        toRecipientsList.add(toRecipients)
        message.toRecipients = toRecipientsList

        message.subject = "[TEST] $subject"

        val body = ItemBody()
        body.contentType = contentType
        body.content = content
        message.body = body

        log.info { "E-post til avsending: $message" }

        try {
            graphClient.users(avsender).sendMail(
                UserSendMailParameterSet
                    .newBuilder()
                    .withMessage(message)
                    .withSaveToSentItems(true)
                    .build(),
            )
                .buildRequest()
                .post()
            log.info { "Mail to $to sent" }
        } catch (e: Exception) {
            log.error(e) { "Got error sending mail: to=$to, subject=$subject, contentType=$contentType, content=$content" }
            // TODO kast feil slik at delene ikke blir markert som rapportert.
        }
    }
}