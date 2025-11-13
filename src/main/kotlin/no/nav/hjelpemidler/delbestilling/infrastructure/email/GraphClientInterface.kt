package no.nav.hjelpemidler.delbestilling.infrastructure.email

interface GraphClientInterface {
    suspend fun sendEmail(recipentEmail: String, subject: String, bodyText: String, contentType: ContentType)
}