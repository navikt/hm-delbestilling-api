package no.nav.hjelpemidler.delbestilling.delbestilling.ikkeSkipedeDelbestillinger

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.common.Lager
import no.nav.hjelpemidler.delbestilling.infrastructure.email.ContentType
import no.nav.hjelpemidler.delbestilling.infrastructure.email.Email
import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction.Transactional

private val log = KotlinLogging.logger {}

class IkkeSkipedeDelbestillingerService(
    private val transaction: Transactional,
    private val email: Email,
) {
    suspend fun genererIkkeSkipedeDelbestillingerRapporter(): List<IkkeSkipetDelbestillingerRapport> {
        val klargjorteDelbestillinger = transaction {
            delbestillingRepository.hentKlargjorteDelbestillinger(30) // TODO: trenger vi å sende inn dager?
        }

        val gruppert = klargjorteDelbestillinger.groupBy { it.enhetnr }

        return gruppert.map {
            IkkeSkipetDelbestillingerRapport(
                lager = Lager.fraLagernummer(it.key),
                delbestillinger = it.value,
            )
        }
    }

    suspend fun sendIkkeSkipedeDelbestillingerRapport(rapport: IkkeSkipetDelbestillingerRapport) {
        log.info { "Sender IkkeSkipetDelbestillingerRapport for lager ${rapport.lager} med delbestillinger ${rapport.delbestillinger}" }
        email.send(rapport.lager.epost(), "TEST: ikke-skipede delbestillinger", rapport.tilHtml(), ContentType.HTML)
    }
}