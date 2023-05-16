package no.nav.hjelpemidler.delbestilling.roller

import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

class RolleService(
    private val client: RolleClient
) {

    suspend fun harDelbestillerRolle(token: String): Boolean {
        val delbestillerResultat = client.hentDelbestillerRolle(token)
        logger.info { "delbestillerResultat: $delbestillerResultat" }
        return delbestillerResultat.kanBestilleDeler
    }
}
