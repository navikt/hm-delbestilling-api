package no.nav.hjelpemidler.delbestilling.infrastructure.outbox

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.infrastructure.kafka.KafkaPublisher
import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction.Transactional
import no.nav.hjelpemidler.delbestilling.infrastructure.slack.Slack
import java.time.LocalDateTime

private val log = KotlinLogging.logger {}

private const val BATCH_SIZE = 100
private const val SLACK_VARSEL_TERSKEL = 5

class OutboxDispatcher(
    private val transactional: Transactional,
    private val kafka: KafkaPublisher,
    private val slack: Slack,
) {

    suspend fun dispatchPending() {
        val meldinger = transactional { outboxDao.hentPending(BATCH_SIZE) }
        if (meldinger.isEmpty()) return

        log.info { "Dispatcher fant ${meldinger.size} pending outbox-meldinger" }

        for (melding in meldinger) {
            try {
                kafka.publish(topic = melding.topic, key = melding.key, payload = melding.payload)
                transactional { outboxDao.markerPublisert(melding.id) }
                log.info { "Outbox-melding ${melding.id} (eventId=${melding.eventId}) publisert" }
            } catch (e: Exception) {
                val nyeAttempts = melding.attempts + 1
                val skalVarsle = nyeAttempts >= SLACK_VARSEL_TERSKEL && !melding.alerted
                log.error(e) { "Publisering av outbox-melding ${melding.id} (eventName=${melding.eventName}) feilet (forsøk $nyeAttempts)" }
                transactional { outboxDao.registrerFeil(melding.id, e.message ?: e.javaClass.name, skalVarsle) }
                if (skalVarsle) {
                    slack.varsleOmOutboxFeil(melding.eventId.toString(), melding.eventName, nyeAttempts)
                }
            }
        }
    }

    suspend fun slettGamlePubliserte(bevarDager: Long = 30) {
        val antall = transactional {
            val tidspunkt = LocalDateTime.now().minusDays(bevarDager)
            outboxDao.slettPubliserteEldreEnn(tidspunkt)
        }
        if (antall > 0) log.info { "Slettet $antall publiserte outbox-rader eldre enn $bevarDager dager" }
    }
}
