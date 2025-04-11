package no.nav.hjelpemidler.hjelpemidlerdigitalSoknadapi.tjenester.norg

import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger {}

class NorgService(private val norgClient: NorgClient) {
    suspend fun hentArbeidsfordelingenhet(kommunenummer: String): ArbeidsfordelingEnhet {
        val enheter = norgClient.hentArbeidsfordelingenheter(kommunenummer)
        if (enheter.size > 1) {
            log.error { "Mottok flere enheter for kommunenummer $kommunenummer: $enheter" }
        }
        return enheter.first()
    }
}
