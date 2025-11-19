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
        transaction {
            val unikeKommunenrUtenEnhet = delbestillingRepository.hentKommunenumreUtenEnhet()

            log.info { "Har funnet ${unikeKommunenrUtenEnhet.size} unike kommunenummer uten enhet, henter lagerenhet for hver av dem" }

            val kommuneNrTilLager = unikeKommunenrUtenEnhet.associateWith { kommunenr ->
                oebs.finnLagerenhet(kommunenr)
            }

            log.info {"kommuneNrTilLager: $kommuneNrTilLager"}

            unikeKommunenrUtenEnhet.forEach { kommunenr ->
                val lager = kommuneNrTilLager[kommunenr]
                if (lager == null) {
                    log.info { "Fant ikke lagerenhet for kommunenummer $kommunenr, hopper over..." }
                    return@forEach
                }

                delbestillingRepository.setEnhetForKommunenummer(kommunenr, lager)
            }

            log.info { "genererEnheter-jobb ferdig" }
        }
    }
}