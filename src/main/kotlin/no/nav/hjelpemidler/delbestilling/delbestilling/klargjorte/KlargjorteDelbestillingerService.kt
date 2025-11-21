package no.nav.hjelpemidler.delbestilling.delbestilling.klargjorte

import no.nav.hjelpemidler.delbestilling.common.Lager
import no.nav.hjelpemidler.delbestilling.infrastructure.email.ContentType
import no.nav.hjelpemidler.delbestilling.infrastructure.email.Email
import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction.Transactional

class KlargjorteDelbestillingerService(
    private val transaction: Transactional,
    private val email: Email,
) {
    suspend fun genererKlargjorteDelbestillingerRapporter(eldreEnnDager: Number): List<KlargjorteDelbestillingerRapport> {
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

    suspend fun sendKlargjorteDelbestillingererRapport(rapport: KlargjorteDelbestillingerRapport): String {
        val melding = rapport.tilHtml()
        email.send(
            recipentEmail = rapport.lager.epost(),
            subject = "Ikke-plukkede delbestillinger", // TODO: subject som const?
            bodyText = melding,
            contentType = ContentType.HTML
        )

        return melding
    }
}