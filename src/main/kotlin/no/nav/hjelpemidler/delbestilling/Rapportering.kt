package no.nav.hjelpemidler.delbestilling

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import no.nav.hjelpemidler.delbestilling.config.isDev
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingService
import no.nav.hjelpemidler.delbestilling.devtools.DevTools
import no.nav.hjelpemidler.delbestilling.infrastructure.leaderElection.ErLeder
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

private val log = KotlinLogging.logger { }

class Rapportering(
    private val delbestillingService: DelbestillingService,
    private val erLeder: ErLeder,
) {

    fun startBakgrunnsjobb(scheduler: ScheduledExecutorService) {
        scheduler.scheduleAtFixedRate({
            runBlocking {
                if (!erLeder()) {
                    log.info { "Hopper over rapporteringsjobb fordi denne instansen ikke er leder." }
                    return@runBlocking
                }

                log.info { "Starter rapporteringsjobb (som leder)..." }

                rapporterAnmodningsBehov()
                log.info { "Rappoerteringsjobber fullført." }
            }
        }, initialDelay(), 1.days.inWholeMilliseconds, TimeUnit.MILLISECONDS)
    }


    private suspend fun rapporterAnmodningsBehov() {
        if (erHelg()) {
            log.info { "Hopper over rapportering av anmodningsbehov fordi det er helg." }
            return
        }

        log.info { "Starter rapportering av anmodningsbehov..." }
        delbestillingService.rapporterDelerTilAnmodning()
        log.info { "Rapportering av anmodningsbehov fullført." }
    }
}

private fun erHelg(): Boolean {
    val iDag = LocalDate.now().dayOfWeek
    return iDag == DayOfWeek.SATURDAY || iDag == DayOfWeek.SUNDAY
}

/**
 * Beregner delay frem til førstkommende tidspunkt kl 01:00
 */
private fun initialDelay(): Long {
    val nå = LocalDateTime.now()

    var startTidspunkt = nå.withHour(1).withMinute(0).withSecond(0).withNano(0)
    if (startTidspunkt <= nå) {
        startTidspunkt = startTidspunkt.plusDays(1)
    }

    return Duration.between(nå, startTidspunkt).toMillis()
}