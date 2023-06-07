package no.nav.hjelpemidler.delbestilling.oebs

import mu.KotlinLogging
import no.nav.hjelpemidler.delbestilling.kafka.KafkaService

private val logg = KotlinLogging.logger {}

private const val OPPRETT_DELBESTILLING_EVENTNAME = "hm-OpprettDelbestilling"

class OebsSinkClient(
    private val kafkaService: KafkaService
) {
    fun sendDelbestilling(opprettBestillingsordreRequest: OpprettBestillingsordreRequest) {
        logg.info { "Sender '$OPPRETT_DELBESTILLING_EVENTNAME'-event for saksnummer '${opprettBestillingsordreRequest.saksnummer}'" }
        try {
            kafkaService.publish(
                key = opprettBestillingsordreRequest.saksnummer,
                eventName = OPPRETT_DELBESTILLING_EVENTNAME,
                value = opprettBestillingsordreRequest,
            )
        } catch (t: Throwable) {
            logg.error(t) { "Sending av delbestilling til Kafka feilet." }
            throw t
        }
    }

}

