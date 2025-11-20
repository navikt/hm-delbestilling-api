package no.nav.hjelpemidler.delbestilling.rapportering

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingService

private val log = KotlinLogging.logger {}

class Rapportering(
    private val jobbScheduler: JobbScheduler,
    private val delbestillingService: DelbestillingService,
    private val månedsrapportAnmodningsbehov: MånedsrapportAnmodningsbehov,
) {

    fun schedulerRapporteringsjobber() {
        jobbScheduler.schedulerGjentagendeJobb(
            navn = "anmodningsbehov",
            jobb = { rapporterAnmodningsbehov() },
            beregnNesteKjøring = { clock -> kl01NesteUkedag(clock) }
        )

        jobbScheduler.schedulerGjentagendeJobb(
            navn = "månedlig_anmodningsoppsummering",
            jobb = { rapporterMånedligAnmodningsoppsummering() },
            beregnNesteKjøring = { clock -> kl0120FørsteDagINesteMåned(clock) }
        )

        jobbScheduler.schedulerGjentagendeJobb(
            navn = "ikke_skipede_delbestillinger",
            jobb = { rapporterIkkeSkipedeDelbestillinger() },
            beregnNesteKjøring = { clock -> kl0130FørsteDagINesteMåned(clock) }
        )
    }

    suspend fun rapporterAnmodningsbehov() {
        delbestillingService.rapporterDelerTilAnmodning()
    }

    suspend fun rapporterMånedligAnmodningsoppsummering() {
        månedsrapportAnmodningsbehov.sendRapporterForForrigeMåned()
    }

    suspend fun rapporterIkkeSkipedeDelbestillinger() {
        delbestillingService.rapporterIkkeSkipedeDelbestillinger()
    }
}