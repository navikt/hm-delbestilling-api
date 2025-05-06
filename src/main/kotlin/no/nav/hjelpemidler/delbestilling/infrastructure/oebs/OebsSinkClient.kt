package no.nav.hjelpemidler.delbestilling.infrastructure.oebs

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.infrastructure.kafka.Kafka


private const val OPPRETT_DELBESTILLING_EVENTNAME = "hm-OpprettDelbestilling"

private val log = KotlinLogging.logger {}

class OebsSinkClient(
    private val kafka: Kafka
) : OebsSink {
    override fun sendDelbestilling(ordre: Ordre) {
        try {
            kafka.publish(
                key = ordre.saksnummer,
                eventName = OPPRETT_DELBESTILLING_EVENTNAME,
                value = ordre,
            )
        } catch (t: Throwable) {
            log.error(t) { "Sending av delbestilling til Kafka feilet." }
            throw t
        }
    }
}
