package no.nav.hjelpemidler.delbestilling.delbestilling

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Oebs
import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction.Transactional

private val log = KotlinLogging.logger {}

class EngangsjobbService(
    private val transaction: Transactional,
    private val oebs: Oebs,
) {
    // Sett enhetnr og enhetnavn på delbestillinger som mangler det
    suspend fun genererEnheter() {
        val unikeKommunenumre = transaction {
            delbestillingRepository.hentKommunenumreUtenEnhet()
        }

        log.info { "Fant ${unikeKommunenumre.size} kommunenummer uten enhet – henter lagerenhet for hver" }

        for (kommunenr in unikeKommunenumre) {
            try {
                val lager = oebs.finnLagerenhet(kommunenr)

                transaction {
                    delbestillingRepository.setEnhetForKommunenummer(kommunenr, lager)
                }
            } catch (e: Exception) {
                log.error(e) { "Kunne ikke sette enhet for kommunenummer $kommunenr – hopper over" }
            }
        }

        log.info { "genererEnheter-jobb ferdig" }
    }
}