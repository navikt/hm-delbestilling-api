package no.nav.hjelpemidler.hjelpemidlerdigitalSoknadapi.tjenester.norg

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.infrastructure.norg.NorgClientInterface

private val log = KotlinLogging.logger {}

class Norg(private val norgClient: NorgClientInterface) {
    suspend fun hentEnhetnummer(kommunenummer: String): String {
        val enheter = norgClient.hentArbeidsfordelingenheter(kommunenummer)
        if (enheter.size > 1) {
            log.error { "Mottok flere enheter for kommunenummer $kommunenummer: $enheter" }
        }
        return enheter.first().enhetNr
    }
}
