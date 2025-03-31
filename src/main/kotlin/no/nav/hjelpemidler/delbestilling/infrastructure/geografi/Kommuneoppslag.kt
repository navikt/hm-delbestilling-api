package no.nav.hjelpemidler.delbestilling.infrastructure.geografi

import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger { }

class Kommuneoppslag(
    private val client: DigihotOppslagClient
) {

    suspend fun kommunenavnOrNull(kommunenr: String): String? {
        return try {
            client.hentKommune(kommunenr).kommunenavn
        } catch (t: Throwable) {
            log.error(t) { "Hening av kommunenavn feilet for kommunenr $kommunenr." }
            null
        }
    }

}
