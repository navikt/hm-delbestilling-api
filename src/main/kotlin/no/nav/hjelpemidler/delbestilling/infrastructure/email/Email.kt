package no.nav.hjelpemidler.delbestilling.infrastructure.email

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.config.isDev

private val log = KotlinLogging.logger {}

class Email(
    val client: GraphClientInterface,
) {

    suspend fun sendSimpleMessage(
        recipentEmail: String,
        subject: String,
        bodyText: String,
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

        if (isDev()) {
            log.info { "Ignorerer utsending av epost i dev." }
            return
        }

        client.sendEmail(recipentEmail = recipentEmail, subject = effectiveSubject, bodyText = bodyText)
        log.info { "post til $recipentEmail sendt." }
    }

    suspend fun sendTestMail(
        recipentEmail: String = "digitalisering.av.hjelpemidler.og.tilrettelegging@nav.no",
        subject: String = "[TEST] hm-delbestilling-api",
        bodyText: String = "Dette er bare en test av epostutsending fra hm-delbestilling-api. Vennligst ignorer meg.",
    ) {
        client.sendEmail(recipentEmail = recipentEmail, subject = subject, bodyText = bodyText)
        log.info { "post til $recipentEmail sendt." }
    }
}