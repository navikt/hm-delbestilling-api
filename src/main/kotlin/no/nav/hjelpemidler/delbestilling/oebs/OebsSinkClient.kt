package no.nav.hjelpemidler.delbestilling.oebs

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.kafka.KafkaService


private const val OPPRETT_DELBESTILLING_EVENTNAME = "hm-OpprettDelbestilling"

private val log = KotlinLogging.logger {}

class OebsSinkClient(
    private val kafkaService: KafkaService
) {
    fun sendDelbestilling(opprettBestillingsordreRequest: OpprettBestillingsordreRequest) {
        log.info { "Sender '$OPPRETT_DELBESTILLING_EVENTNAME'-event for saksnummer '${opprettBestillingsordreRequest.saksnummer}'" }
        try {
            kafkaService.publish(
                key = opprettBestillingsordreRequest.saksnummer,
                eventName = OPPRETT_DELBESTILLING_EVENTNAME,
                value = opprettBestillingsordreRequest,
            )
        } catch (t: Throwable) {
            log.error(t) { "Sending av delbestilling til Kafka feilet." }
            throw t
        }
    }
}
