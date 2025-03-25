package no.nav.hjelpemidler.delbestilling.oebs

import no.nav.hjelpemidler.delbestilling.infrastructure.monitoring.Logg
import no.nav.hjelpemidler.delbestilling.kafka.KafkaService


private const val OPPRETT_DELBESTILLING_EVENTNAME = "hm-OpprettDelbestilling"

class OebsSinkClient(
    private val kafkaService: KafkaService
) {
    fun sendDelbestilling(opprettBestillingsordreRequest: OpprettBestillingsordreRequest) {
        Logg.info { "Sender '$OPPRETT_DELBESTILLING_EVENTNAME'-event for saksnummer '${opprettBestillingsordreRequest.saksnummer}'" }
        try {
            kafkaService.publish(
                key = opprettBestillingsordreRequest.saksnummer,
                eventName = OPPRETT_DELBESTILLING_EVENTNAME,
                value = opprettBestillingsordreRequest,
            )
        } catch (t: Throwable) {
            Logg.error(t) { "Sending av delbestilling til Kafka feilet." }
            throw t
        }
    }
}
