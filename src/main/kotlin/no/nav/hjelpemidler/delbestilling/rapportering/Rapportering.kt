package no.nav.hjelpemidler.delbestilling.rapportering

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.config.isProd
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingService

private val log = KotlinLogging.logger {}

class Rapportering(
    private val jobbScheduler: JobbScheduler,
    private val delbestillingService: DelbestillingService,
    private val månedsrapportAnmodningsbehov: MånedsrapportAnmodningsbehov,
    ) {

    fun schedulerRapporteringsjobber() {
        jobbScheduler.schedulerGjentagendeJobb(
            "anmodningsbehov",
            { rapporterAnmodningsbehov() },
            { clock -> kl01NesteUkedag(clock) })

        jobbScheduler.schedulerGjentagendeJobb(
            "månedlig_anmodningsoppsummering",
            { rapporterMånedligAnmodningsoppsummering() },
            { clock -> kl0120FørsteDagINesteMåned(clock) }
        )
    }

    suspend fun rapporterAnmodningsbehov() {
        delbestillingService.rapporterDelerTilAnmodning()
    }

    suspend fun rapporterMånedligAnmodningsoppsummering() {
        if (isProd()) {
            log.info { "Skipper månedsrapportering av anmodningsbehov i prod inntil det er verifisert i dev." }
        } else {
            månedsrapportAnmodningsbehov.sendRapporterForForrigeMåned()
        }
    }
}