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
            navn = "anmodningsbehov",
            jobb = { rapporterAnmodningsbehov() },
            beregnNesteKjøring = { clock -> kl01NesteUkedag(clock) }
        )

        jobbScheduler.schedulerGjentagendeJobb(
            navn = "månedlig_anmodningsoppsummering",
            jobb = { rapporterMånedligAnmodningsoppsummering() },
            //beregnNesteKjøring = { clock -> kl0120FørsteDagINesteMåned(clock) }
            // TODO bytt til en gang i måneden før email utsending skrus på i prod
            beregnNesteKjøring = { clock -> kl0120HverNatt(clock) }
        )
    }

    suspend fun rapporterAnmodningsbehov() {
        delbestillingService.rapporterDelerTilAnmodning()
    }

    suspend fun rapporterMånedligAnmodningsoppsummering() {
        månedsrapportAnmodningsbehov.sendRapporterForForrigeMåned()
    }
}