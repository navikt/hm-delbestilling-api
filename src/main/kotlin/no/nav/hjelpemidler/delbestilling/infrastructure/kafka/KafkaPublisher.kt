package no.nav.hjelpemidler.delbestilling.infrastructure.kafka

interface KafkaPublisher {
    fun publish(topic: String, key: String, payload: String)
}
