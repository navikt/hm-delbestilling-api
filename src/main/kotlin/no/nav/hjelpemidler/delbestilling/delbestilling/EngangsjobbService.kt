package no.nav.hjelpemidler.delbestilling.delbestilling

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Oebs
import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction.Transactional

private val log = KotlinLogging.logger {}

// Enkel wrapping rundt Lager
data class LagerEnhet(
    val nummer: String,
    val navn: String,
)

class EngangsjobbService(
    private val transaction: Transactional,
    private val oebs: Oebs,
) {
    suspend fun finnEnheterTilKommunenumre(): Map<String, LagerEnhet?> {
        val unikeKommunenumre = transaction {
            delbestillingRepository.hentKommunenumreUtenEnhet()
        }

        log.info { "Fant ${unikeKommunenumre.size} kommunenummer uten enhet – henter lagerenhet for hver" }

        val kommuneNrTilLager = unikeKommunenumre.associateWith { kommunenummer ->
            try {
                val lager = oebs.finnLagerenhet(kommunenummer)
                LagerEnhet(lager.nummer, lager.navn)
            } catch (e: Exception) {
                log.error(e) { "Fant ikke lagerenhet for kommunenummer $kommunenummer, returnerer null" }
                null
            }
        }

        return kommuneNrTilLager
    }
}