package no.nav.hjelpemidler.delbestilling.infrastructure.geografi

import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger { }

class Kommuneoppslag(
    private val oppslagClient: OppslagClient
) {

    suspend fun kommunenavnOrNull(kommunenr: String): String? {
        try {
            return oppslagClient.hentKommune(kommunenr).kommunenavn
        } catch (e: Exception) {
            log.error(e) { "Henting av kommunenavn feilet for kommunenr '$kommunenr'." }
            throw e
        }
    }
}
