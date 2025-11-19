package no.nav.hjelpemidler.delbestilling.delbestilling

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.common.Lager
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Oebs
import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction.Transactional

private val log = KotlinLogging.logger {}

class EngangsjobbService(
    private val transaction: Transactional,
    private val oebs: Oebs,
) {
    suspend fun finnEnhetTilKommunenumre(): Map<String, LagerEnhet?> {
        val unikeKommunenumre = transaction {
            delbestillingRepository.hentKommunenumreUtenEnhet()
        }

        log.info { "Fant ${unikeKommunenumre.size} kommunenummer uten enhet – henter lagerenhet for hver" }

        // Enkel wrapping rundt Lager
        data class LagerEnhet(
            val nummer: String,
            val navn: String,
        )

        val kommuneNrTilLager = unikeKommunenumre.associateWith { kommunenummer ->
            try {
                val lager = oebs.finnLagerenhet(kommunenummer)
                LagerEnhet(lager.nummer, lager.navn)
            } catch (e: Exception) {
                log.error(e) { "Fant ikke lagerenhet for kommunenummer $kommunenummer, returnerer null" }
                null
            }
        }

        log.info { "kommuneNrTilLager: $kommuneNrTilLager" }

        return kommuneNrTilLager

        /*
        var antallOppdatert = 0
        var antallFeilet = 0
        for (kommunenr in unikeKommunenumre) {
            try {
                val lager = oebs.finnLagerenhet(kommunenr)

                transaction {
                    log.info { "Oppdaterer $kommunenr med lager $lager..." }
                    delbestillingRepository.setEnhetForKommunenummer(kommunenr, lager)
                }

                antallOppdatert++
            } catch (e: Exception) {
                log.error(e) { "Kunne ikke sette enhet for kommunenummer $kommunenr – hopper over" }
                antallFeilet++
            }
        }


        log.info { "genererEnheter-jobb ferdig, antallOppdatert=$antallOppdatert, antallFeilet=$antallFeilet" }
        */
    }
}