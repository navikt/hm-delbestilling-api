package no.nav.hjelpemidler.delbestilling.infrastructure.epostoutbox

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.config.AppConfig
import no.nav.hjelpemidler.delbestilling.config.isDev
import no.nav.hjelpemidler.delbestilling.infrastructure.email.ContentType
import no.nav.hjelpemidler.delbestilling.infrastructure.email.Email
import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction.Transactional
import no.nav.hjelpemidler.delbestilling.infrastructure.slack.Slack
import java.time.Clock
import java.time.LocalDateTime

private val log = KotlinLogging.logger {}
private const val BATCH_SIZE = 100
private const val SLACK_VARSEL_TERSKEL = 5
private const val BEVAR_SENDTE_EPOSTER_DAGER = 90L

class EpostOutboxDispatcher(
    private val transaction: Transactional,
    private val email: Email,
    private val slack: Slack,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    suspend fun dispatchPending() {
        val meldinger = transaction { epostOutboxDao.hentPending(BATCH_SIZE) }

        for (melding in meldinger) {
            try {
                email.send(melding.mottaker, melding.emne, melding.html, ContentType.HTML)
                transaction { epostOutboxDao.markerSendt(melding.id) }
            } catch (e: Exception) {
                val nyeAttempts = melding.attempts + 1
                val skalVarsle = nyeAttempts >= SLACK_VARSEL_TERSKEL && !melding.alerted
                log.error(e) { "E-postutsending av outbox-melding ${melding.id} feilet (forsøk $nyeAttempts)" }
                transaction { epostOutboxDao.registrerFeil(melding.id, e.message ?: e.javaClass.name, skalVarsle) }
                if (skalVarsle) slack.varsleOmOutboxFeil("epost-${melding.id}", "Epost", nyeAttempts)
            }
        }
    }

    suspend fun slettGamleSendteEposter(bevarDager: Long = BEVAR_SENDTE_EPOSTER_DAGER) {
        val antall = transaction {
            val tidspunkt = LocalDateTime.now(clock).minusDays(bevarDager)
            epostOutboxDao.slettSendteEldreEnn(tidspunkt)
        }
        if (antall > 0) log.info { "Slettet $antall sendte e-postoutbox-rader eldre enn $bevarDager dager" }
    }
}