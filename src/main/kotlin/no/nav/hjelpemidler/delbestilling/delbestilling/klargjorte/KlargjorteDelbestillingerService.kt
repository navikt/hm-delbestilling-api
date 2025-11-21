package no.nav.hjelpemidler.delbestilling.delbestilling.klargjorte

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.common.Lager
import no.nav.hjelpemidler.delbestilling.infrastructure.email.ContentType
import no.nav.hjelpemidler.delbestilling.infrastructure.email.Email
import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction.Transactional

private val log = KotlinLogging.logger {}

class KlargjorteDelbestillingerService(
    private val transaction: Transactional,
    private val email: Email,
) {
    suspend fun genererKlargjorteDelbestillingerRapporter(): List<KlargjorteDelbestillingerRapport> {
        val klargjorteDelbestillinger = transaction {
            delbestillingRepository.hentKlargjorteDelbestillinger(30) // TODO: trenger vi å sende inn dager?
        }

        val gruppert = klargjorteDelbestillinger.groupBy { it.enhetnr }

        return gruppert.map {
            KlargjorteDelbestillingerRapport(
                lager = Lager.fraLagernummer(it.key),
                delbestillinger = it.value,
            )
        }
    }

    suspend fun sendKlargjorteDelbestillingererRapport(rapport: KlargjorteDelbestillingerRapport): String {
        val melding = rapport.tilHtml()
        email.send(
            recipentEmail = rapport.lager.epost(),
            subject = "Ikke-plukke delbestillinger", // TODO: subject som const?
            bodyText = melding,
            contentType = ContentType.HTML
        )

        return melding
    }
}