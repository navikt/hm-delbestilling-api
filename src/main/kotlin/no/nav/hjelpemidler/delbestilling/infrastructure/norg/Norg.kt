package no.nav.hjelpemidler.delbestilling.infrastructure.norg

import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger {}

class Norg(
    private val norgClient: NorgClientInterface,
) {

    suspend fun hentEnhetnummer(kommunenummer: String): String {
        // TODO Denne kan godt ta i bruk cache
        val enheter = norgClient.hentArbeidsfordelingenheter(kommunenummer)
        if (enheter.size > 1) {
            log.error { "Mottok flere enheter for kommunenummer $kommunenummer: $enheter" }
        }
        return enheter.first().enhetNr
    }
}