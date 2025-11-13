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
            // TODO skru på igjen denne beregnNesteKjøring = { clock -> kl01NesteUkedag(clock) })
            beregnNesteKjøring = { clock -> hvert10MinuttIDev(clock) })

        jobbScheduler.schedulerGjentagendeJobb(
            navn = "månedlig_anmodningsoppsummering",
            jobb = { rapporterMånedligAnmodningsoppsummering() },
            // TODO skru på igjen denne beregnNesteKjøring = { clock -> kl0120FørsteDagINesteMåned(clock) }
            beregnNesteKjøring = { clock -> hvert10MinuttIDev(clock) }

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