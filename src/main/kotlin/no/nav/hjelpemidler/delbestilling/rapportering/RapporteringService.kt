package no.nav.hjelpemidler.delbestilling.rapportering

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.common.Lager
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingService
import java.time.LocalDate
import java.time.YearMonth

private val log = KotlinLogging.logger {}

class RapporteringService(
    private val delbestillingService: DelbestillingService,
    private val månedsrapportAnmodning: MånedsrapportAnmodning,
) {

    suspend fun startRapporteringsjobber() {
        log.info { "Starter rapporteringsjobber..." }

        sendAnmodningsbehov()
        sendMånedsrapporterOmAnmodninger()

        log.info { "Rapportering fullført." }
    }

    private suspend fun sendAnmodningsbehov() {
        delbestillingService.rapporterDelerTilAnmodning()
    }

    private fun sendMånedsrapporterOmAnmodninger() {
        val erFørsteDagIMåneden = LocalDate.now().dayOfMonth == 1
        if (!erFørsteDagIMåneden) {
            log.info { "Hopper over månedsrapportering. Utføres kun den 1. i hver måned." }
            return
        }

        val forrigeMåned = YearMonth.now().minusMonths(1)
        Lager.entries.forEach { lager ->
            månedsrapportAnmodning.sendRapport(lager, forrigeMåned)
        }
    }
}

