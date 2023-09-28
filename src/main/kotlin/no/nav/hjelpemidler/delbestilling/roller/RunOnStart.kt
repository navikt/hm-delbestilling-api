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
                    logg.info { "Klarte ikke å finne navn for hmsnr: ${lagretDelbestilling.delbestilling.hmsnr} (saksnummer: ${lagretDelbestilling.saksnummer}), hoppper over..." }
                    return@forEach
                }

                if (lagretDelbestilling.delbestilling.navn != null) {
                    logg.info { "lagretBestilling med saksnummer ${lagretDelbestilling.saksnummer} har allerede navn=${lagretDelbestilling.delbestilling.navn}, hopper over..." }
                    return@forEach
                }

                val oppdatertDelbestilling = lagretDelbestilling.delbestilling.copy(navn = navnHovedprodukt)

                logg.info { "Oppdater navn til \"$navnHovedprodukt\" for ${lagretDelbestilling.delbestilling.hmsnr} " }

                delbestillingRepository.oppdaterDelbestillingUtenSistOppdatert(
                    tx,
                    lagretDelbestilling.saksnummer,
                    oppdatertDelbestilling
                )
                logg.info("Oppdatert delbestilling for saksnummer ${lagretDelbestilling.saksnummer}: $oppdatertDelbestilling")
                antallOppdaterteRader++
            }

            logg.info("Rader oppdatert: $antallOppdaterteRader")
        }
    }
}
