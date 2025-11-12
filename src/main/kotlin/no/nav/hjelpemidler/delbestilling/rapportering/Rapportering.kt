package no.nav.hjelpemidler.delbestilling.rapportering

import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingService


class Rapportering(
    private val jobbScheduler: JobbScheduler,
    private val delbestillingService: DelbestillingService,
    ) {

    fun schedulerRapporteringsjobber() {
        jobbScheduler.schedulerGjentagendeJobb(
            "anmodningsbehov",
            { rapporterAnmodningsbehov() },
            { clock -> kl01NesteUkedag(clock) })

        jobbScheduler.schedulerGjentagendeJobb(
            "månedlig_anmodningsoppsummering",
            { rapporterMånedligAnmodningsoppsummering() },
            { clock -> kl01FørsteDagINesteMåned(clock) }
        )
    }

    suspend fun rapporterAnmodningsbehov() {
        delbestillingService.rapporterDelerTilAnmodning()
    }

    suspend fun rapporterMånedligAnmodningsoppsummering() {
        TODO()
    }
}