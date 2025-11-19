package no.nav.hjelpemidler.delbestilling.delbestilling

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Oebs
import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction.Transactional

private val log = KotlinLogging.logger {}

class EngangsjobbService(
    private val transaction: Transactional,
    private val oebs: Oebs,
) {
    suspend fun genererEnheter() {
        transaction {
            val delbestillingerUtenEnhet = delbestillingRepository.hentDelbestillingerUtenEnhet()

            val unikeKommunenr = delbestillingerUtenEnhet.map { it.brukersKommunenummer }.distinct()

            log.info { "Har funnet ${unikeKommunenr.size} unike kommunenummer, henter lagerenhet for hver av dem" }

            val kommuneNrTilLager = unikeKommunenr.associateWith { kommunenr ->
                oebs.finnLagerenhet(kommunenr)
            }

            log.info {"kommuneNrTilLager: $kommuneNrTilLager"}

            unikeKommunenr.forEach { kommunenr ->
                val lager = kommuneNrTilLager[kommunenr]
                if (lager == null) {
                    log.info { "Fant ikke lagerenhet for kommunenummer $kommunenr, hopper over..." }
                    return@forEach
                }

                delbestillingRepository.setEnhetForKommunenummer(kommunenr, lager)
            }

            log.info { "Jobb ferdig" }
        }
    }
}