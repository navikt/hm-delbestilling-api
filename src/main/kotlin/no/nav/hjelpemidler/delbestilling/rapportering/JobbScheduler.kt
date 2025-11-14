package no.nav.hjelpemidler.delbestilling.rapportering

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import no.nav.hjelpemidler.delbestilling.infrastructure.leaderElection.ErLeder
import java.time.Clock
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

private val log = KotlinLogging.logger { }

class JobbScheduler(
    private val scheduler: ScheduledExecutorService,
    private val erLeder: ErLeder,
    private val clock: Clock,
) {

    /**
     *  Sørger for at jobber kjører på ønsket tidspunkt. I motsetning til scheduler.scheduleAtFixedRate
     *  som sørger for at jobbene kjører med fast intervall, men som kan avvike fra ønsket kjøretidspunkt pga
     *  enrdringer som f.eks. sommertid.
     *
     *  Sørger også for at det er kun lederen (leader election) som kjører jobbene, slik at vi unngår duplikater.
     */
    fun schedulerGjentagendeJobb(
        navn: String,
        jobb: suspend CoroutineScope.() -> Unit,
        beregnNesteKjøring: (Clock) -> LocalDateTime
    ) {
        val task = Runnable {
            runBlocking {
                kjørJobb(navn, jobb)
            }

            // Reschedule neste kjøring til ønsket tidspunkt.
            schedulerGjentagendeJobb(navn, jobb, beregnNesteKjøring)
        }

        val nesteKjøring = beregnNesteKjøring(clock)
        val forsinkelseTilNesteKjøring = Duration.between(
            LocalDateTime.now(clock),
            nesteKjøring
        ).toMillis()

        require(forsinkelseTilNesteKjøring > 0) { "Kan ikke ha negativ forsinkelse til neste kjøring. Navn=$navn, forsinkelse=$forsinkelseTilNesteKjøring, nesteKjøring=$nesteKjøring" }

        log.info { "Schedulerer neste kjøring av $navn til $nesteKjøring (delay=$forsinkelseTilNesteKjøring)" }
        scheduler.schedule(task, forsinkelseTilNesteKjøring, TimeUnit.MILLISECONDS)
    }

    suspend fun CoroutineScope.kjørJobb(navn: String, jobb: suspend CoroutineScope.() -> Unit) {
        if (!erLeder()) {
            log.info { "Hopper over jobb $navn fordi denne instansen ikke er leder." }
            return
        }

        log.info { "Starter jobb $navn..." }
        try {
            jobb()
            log.info { "Jobb $navn fullført." }
        } catch (e: Exception) {
            log.error(e) { "Jobb $navn feilet." }
        }
    }
}