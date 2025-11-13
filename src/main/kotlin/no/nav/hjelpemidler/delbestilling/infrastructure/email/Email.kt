package no.nav.hjelpemidler.delbestilling.infrastructure.email

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.config.isDev

private val log = KotlinLogging.logger {}

class Email(
    val client: GraphClientInterface,
) {

    suspend fun send(
        recipentEmail: String,
        subject: String,
        bodyText: String,
        contentType: ContentType,
    ) {
        val effectiveSubject = if (isDev()) "[TEST] $subject" else subject

        log.info {
            """
            E-post til avsending:
            To: $recipentEmail
            Subject: $effectiveSubject
            Body: $bodyText
        """.trimIndent()
        }

        /*
        if (isDev()) {
            log.info { "Ignorerer utsending av epost i dev." }
            return
        }
         */

        client.sendEmail(
            recipentEmail = recipentEmail,
            subject = effectiveSubject,
            bodyText = bodyText,
            contentType = contentType
        )
        log.info { "E-post til $recipentEmail sendt." }
    }
}