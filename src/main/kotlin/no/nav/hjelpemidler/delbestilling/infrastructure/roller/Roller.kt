package no.nav.hjelpemidler.delbestilling.infrastructure.roller

import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger { }

class Roller(private val client: RollerClient) {

    suspend fun hentDelbestillerRolle(token: String): Delbestiller {
        return try {
            log.info { "Henter delbestillerrolle." }
            client.hentDelbestillerRolle(token).delbestillerrolle
        } catch (e: Exception) {
            log.error(e) { "Henting av delbestillerrolle feilet." }
            throw e
        }
    }
}
