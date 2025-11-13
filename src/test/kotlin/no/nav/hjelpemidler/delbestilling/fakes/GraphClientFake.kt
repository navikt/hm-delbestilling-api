package no.nav.hjelpemidler.delbestilling.fakes

import no.nav.hjelpemidler.delbestilling.infrastructure.email.ContentType
import no.nav.hjelpemidler.delbestilling.infrastructure.email.GraphClientInterface

class GraphClientFake : GraphClientInterface {

    val outbox = mutableListOf<SendtEmail>()

    override suspend fun sendEmail(recipentEmail: String, subject: String, bodyText: String, contentType: ContentType) {
        outbox.add(SendtEmail(recipentEmail, subject, bodyText, contentType))
    }

}

data class SendtEmail(
    val recipent: String,
    val subject: String,
    val body: String,
    val contentType: ContentType
)