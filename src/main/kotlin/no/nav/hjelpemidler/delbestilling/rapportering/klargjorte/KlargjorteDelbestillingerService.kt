package no.nav.hjelpemidler.delbestilling.rapportering.klargjorte

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.common.Lager
import no.nav.hjelpemidler.delbestilling.infrastructure.email.ContentType
import no.nav.hjelpemidler.delbestilling.infrastructure.email.Email
import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction.Transactional
import no.nav.hjelpemidler.delbestilling.infrastructure.slack.Slack

private val log = KotlinLogging.logger {}

const val KLARGJORTE_DELBESTILLINGER_SUBJECT = "Ikke-plukkede delbestillinger"

class KlargjorteDelbestillingerService(
    private val transaction: Transactional,
    private val email: Email,
    private val slack: Slack,
) {
    suspend fun genererKlargjorteDelbestillingerRapporter(eldreEnnDager: Int): List<KlargjorteDelbestillingerRapport> {
        val klargjorteDelbestillinger = transaction {
            delbestillingRepository.hentKlargjorteDelbestillinger(eldreEnnDager)
        }

        val gruppert = klargjorteDelbestillinger.groupBy { it.enhetnr }

        return gruppert.map {
            KlargjorteDelbestillingerRapport(
                lager = Lager.fraLagernummer(it.key),
                delbestillinger = it.value,
            )
        }
    }

    suspend fun rapporterKlargjorteDelbestillinger(eldreEnnDager: Int): List<KlargjorteDelbestillingerRapport> {
        try {
            val rapporter = genererKlargjorteDelbestillingerRapporter(eldreEnnDager)

            if (rapporter.isEmpty()) {
                log.info { "Ingen rapporter for klargjorte delbestillinger generert. Ingenting sendes." }
            }

            rapporter.forEach {
                sendKlargjorteDelbestillingerRapport(it)
            }

            return rapporter
        } catch (t: Throwable) {
            log.error(t) { "Rapportering av klargjorte delbestillinger feilet." }
            slack.varsleOmRapporteringKlargjorteDelbestillingerFeilet()
            throw t
        }
    }

    suspend fun sendKlargjorteDelbestillingerRapport(rapport: KlargjorteDelbestillingerRapport): String {
        val melding = rapport.tilHtml()
        email.send(
            recipentEmail = rapport.lager.epost(),
            subject = KLARGJORTE_DELBESTILLINGER_SUBJECT,
            bodyText = melding,
            contentType = ContentType.HTML
        )

        return melding
    }
}