package no.nav.hjelpemidler.delbestilling.infrastructure.norg

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.common.Enhet
import no.nav.hjelpemidler.delbestilling.infrastructure.slack.Slack

private val log = KotlinLogging.logger {}

class Norg(
    private val norgClient: NorgClientInterface,
    private val slack: Slack,
) {

    private suspend fun hentEnhetnummer(kommunenummer: String): String {
        val enheter = norgClient.hentArbeidsfordelingenheter(kommunenummer)
        if (enheter.size > 1) {
            log.error { "Mottok flere enheter for kommunenummer $kommunenummer: $enheter" }
        }
        return enheter.first().enhetNr
    }

    suspend fun hentEnhet(kommunenummer: String): Enhet {
        val enhetsnummer = hentEnhetnummer(kommunenummer)

        return try {
            when (enhetsnummer) {
                // 4702 = HMS Akershus, men når det gjelder lagerstatus så håndteres dette av Oslo.
                // I OeBS skal innbyggere her være knyttet til 4703 HMS Oslo
                ENHETSNUMMER_HMS_AKERSHUS -> Enhet.OSLO
                else -> Enhet.fraEnhetsnummer(enhetsnummer)
            }
        } catch (e: Exception) {
            log.error(e) { "Klarte ikke finne enhet for kommunenummer $kommunenummer" }
            slack.varsleOmUkjentEnhet(kommunenummer, enhetsnummer)
            throw e
        }
    }
}

private const val ENHETSNUMMER_HMS_AKERSHUS = "4702"