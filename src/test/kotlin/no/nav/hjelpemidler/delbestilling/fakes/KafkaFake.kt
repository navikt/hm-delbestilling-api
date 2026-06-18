package no.nav.hjelpemidler.delbestilling.fakes

import no.nav.hjelpemidler.delbestilling.infrastructure.jsonMapper
import no.nav.hjelpemidler.delbestilling.infrastructure.kafka.KafkaPublisher

data class PublisertMelding(val topic: String, val key: String, val payload: String) {
    val eventName: String? get() = runCatching {
        jsonMapper.readTree(payload)["eventName"]?.asString()
    }.getOrNull()
}

class KafkaFake : KafkaPublisher {
    private val _publiserte = mutableListOf<PublisertMelding>()
    val publiserte: List<PublisertMelding> get() = _publiserte

    var skalKasteFeil = false
    var feil: Throwable = RuntimeException("Kafka fake feil")

    override fun publish(topic: String, key: String, payload: String) {
        if (skalKasteFeil) throw feil
        _publiserte.add(PublisertMelding(topic, key, payload))
    }

    fun clear() {
        _publiserte.clear()
    }
}
