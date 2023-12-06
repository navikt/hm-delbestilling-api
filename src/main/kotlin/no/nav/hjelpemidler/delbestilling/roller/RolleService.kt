package no.nav.hjelpemidler.delbestilling.roller

import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

class RolleService(
    private val client: RolleClient,
) {

    suspend fun hentDelbestillerRolle(token: String): Delbestiller {
        return client.hentDelbestillerRolle(token).delbestillerrolle
    }
}
