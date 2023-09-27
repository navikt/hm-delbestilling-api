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

            var antallOppdaterteRader = 0
            delbestillinger.forEach { lagretDelbestilling ->

                val navnHovedprodukt =
                    HjelpemiddelDeler.hentHjelpemiddelMedDeler(lagretDelbestilling.delbestilling.hmsnr)?.navn

                if (navnHovedprodukt == null) {
                    logg.info { "Klarte ikke å finne navn for ${lagretDelbestilling.delbestilling.hmsnr}, hoppper over..." }
                    return@forEach
                }

                if (lagretDelbestilling.delbestilling.navn != null) {
                    logg.info { "lagretBestilling med saksnummer ${lagretDelbestilling.saksnummer} har allerede navn=${lagretDelbestilling.delbestilling.navn}, hopper over..." }
                    return@forEach
                }

                delbestillingRepository.withTransaction { tx ->
                    val oppdatertDelbestilling = lagretDelbestilling.delbestilling.copy(navn = navnHovedprodukt)
                    // TODO: for debugging, må fjernes før merges til prod
                    val oppdaterSaksnummer = "46"
                    if (lagretDelbestilling.saksnummer == oppdaterSaksnummer.toLong()) {
                        logg.info { "Oppdater navn til \"$navnHovedprodukt\" for ${lagretDelbestilling.delbestilling.hmsnr} " }
                        delbestillingRepository.oppdaterDelbestillingUtenSistOppdatert(
                            tx,
                            lagretDelbestilling.saksnummer,
                            oppdatertDelbestilling
                        )
                        logg.info("Oppdatert delbestilling for saksnummer $oppdaterSaksnummer: $oppdatertDelbestilling")
                        antallOppdaterteRader++
                    }
                }
            }

            logg.info("Rader oppdatert: $antallOppdaterteRader")
        }
    }
}
