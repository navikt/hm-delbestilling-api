package no.nav.hjelpemidler.delbestilling.rapportering

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingService
import no.nav.hjelpemidler.delbestilling.infrastructure.email.ContentType
import no.nav.hjelpemidler.delbestilling.infrastructure.email.Email
import no.nav.hjelpemidler.delbestilling.rapportering.klargjorte.KlargjorteDelbestillingerService
import java.time.LocalDateTime

private val log = KotlinLogging.logger {}

class Rapportering(
    private val jobbScheduler: JobbScheduler,
    private val delbestillingService: DelbestillingService,
    private val klargjorteDelbestillingerService: KlargjorteDelbestillingerService,
    private val månedsrapportAnmodningsbehov: MånedsrapportAnmodningsbehov,
    private val email: Email
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
            navn = "klargjorte_delbestillinger",
            jobb = { rapporterKlargjorteDelbestillinger() },
            beregnNesteKjøring = { clock -> kl0130FørsteDagINesteMåned(clock) }
        )

        jobbScheduler.schedulerEngangsjobb(
            navn = "test_epost",
            jobb = {
                email.send(
                    recipentEmail = "ole.steinar.lillestol.skrede@nav.no",
                    subject = "TEST",
                    contentType = ContentType.TEXT,
                    bodyText = """
                        Hei!
                        
                        Dette er en test.
                    """.trimIndent(),
                )
            },
            beregnNesteKjøring = { LocalDateTime.now().plusMinutes(2) }
        )
    }

    suspend fun rapporterAnmodningsbehov() {
        delbestillingService.rapporterDelerTilAnmodning()
    }

    suspend fun rapporterMånedligAnmodningsoppsummering() {
        månedsrapportAnmodningsbehov.sendRapporterForForrigeMåned()
    }

    suspend fun rapporterKlargjorteDelbestillinger() {
        klargjorteDelbestillingerService.rapporterKlargjorteDelbestillinger(eldreEnnDager = 30)
    }
}