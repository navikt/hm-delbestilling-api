package no.nav.hjelpemidler.delbestilling.roller

import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

class RolleService(
    private val client: RolleClient,
) {

    suspend fun hentDelbestillerRolle(token: String): DelbestillerResponse {
        return client.hentDelbestillerRolle(token)
    }
}
