package no.nav.hjelpemidler.delbestilling.roller

import mu.KotlinLogging
import no.nav.hjelpemidler.delbestilling.Database
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingRepository
import no.nav.hjelpemidler.delbestilling.hjelpemidler.HjelpemiddelDeler

private val logg = KotlinLogging.logger {}

class RunOnStart(
    private val delbestillingRepository: DelbestillingRepository = DelbestillingRepository(Database.migratedDataSource),
) {
    suspend fun importNavn() {
        delbestillingRepository.withTransaction { tx ->
            val delbestillinger = delbestillingRepository.hentDelbestillinger(tx)

            delbestillinger.forEach { lagretDelbestilling ->
                logg.info { "opprinnelig delbestilling: $lagretDelbestilling" }

                val navnHovedprodukt =
                    HjelpemiddelDeler.hentHjelpemiddelMedDeler(lagretDelbestilling.delbestilling.hmsnr)?.navn

                logg.info { "navnHovedprodukt: $navnHovedprodukt" }

                if (lagretDelbestilling.delbestilling.navn == null && navnHovedprodukt != null) {
                    delbestillingRepository.withTransaction { tx ->
                        val oppdatertDelbestilling = lagretDelbestilling.delbestilling.copy(navn = navnHovedprodukt)
                        if (lagretDelbestilling.saksnummer == "47".toLong()) {
                            delbestillingRepository.oppdaterDelbestillingMedNavn(
                                tx,
                                lagretDelbestilling.saksnummer,
                                oppdatertDelbestilling
                            )
                            logg.info("Oppdatert delbestilling for saksnummer 47: $oppdatertDelbestilling")
                        }
                    }
                }

                logg.info("----------------------------")
            }
        }
    }
}
