package no.nav.hjelpemidler.delbestilling.rapportering

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingService
import no.nav.hjelpemidler.delbestilling.infrastructure.leaderElection.ErLeder
import java.time.Clock
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

private val log = KotlinLogging.logger { }


class Rapportering(
    private val delbestillingService: DelbestillingService,
    private val erLeder: ErLeder,
    private val clock: Clock
) {

    fun scheduleRapporteringsjobb(scheduler: ScheduledExecutorService) {
        scheduler.scheduleAtFixedRate({
            runBlocking {
                kjørRapporteringsjobb()
            }
        }, delayTilKl01(clock), ETT_DØGN, TimeUnit.MILLISECONDS)
    }

    suspend fun kjørRapporteringsjobb() {
        if (!erLeder()) {
            log.info { "Hopper over rapporteringsjobb fordi denne instansen ikke er leder." }
            return
        }

        log.info { "Starter rapporteringsjobb (som leder)..." }

        rapporterAnmodningsBehov()
        log.info { "Rappoerteringsjobber fullført." }
    }


    private suspend fun rapporterAnmodningsBehov() {
        if (erHelg(clock)) {
            log.info { "Hopper over rapportering av anmodningsbehov fordi det er helg." }
            return
        }

        log.info { "Starter rapportering av anmodningsbehov..." }
        delbestillingService.rapporterDelerTilAnmodning()
        log.info { "Rapportering av anmodningsbehov fullført." }
    }
}