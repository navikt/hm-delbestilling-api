package no.nav.hjelpemidler.delbestilling.kafka

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import no.nav.hjelpemidler.delbestilling.Config.kafkaProducerProperties
import no.nav.hjelpemidler.delbestilling.infrastructure.monitoring.Logg
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.time.LocalDateTime
import java.util.Properties
import java.util.UUID
import java.util.concurrent.TimeUnit


class KafkaService(
    properties: Properties = kafkaProducerProperties,
    private val producer: Producer<String, String> = createProducer(properties),
) {

    init {
        Runtime.getRuntime().addShutdownHook(Thread(::shutdownHook))
    }

    fun hendelseOpprettet(
        measurement: String,
        fields: Map<String, Any>,
        tags: Map<String, String>,
    ) {
        publish(
            measurement,
            jsonMapper.writeValueAsString(
                mapOf(
                    "eventId" to UUID.randomUUID(),
                    "eventName" to "hm-bigquery-sink-hendelse",
                    "schemaId" to "hendelse_v2",
                    "payload" to mapOf(
                        "opprettet" to LocalDateTime.now(),
                        "navn" to measurement,
                        "kilde" to "hm-delbestilling-api",
                        "data" to fields.mapValues { it.value.toString() }
                            .plus(tags)
                            .filterKeys { it != "counter" }
                    )
                )
            )
        )
    }

    fun publish(key: String, eventName: String, value: Any) {
        val event = jsonMapper.valueToTree<ObjectNode>(value)
            .put("eventName", eventName)
            .put("eventId", UUID.randomUUID().toString())
        val eventJson = jsonMapper.writeValueAsString(event)
        publish(key, eventJson)
    }

    private fun publish(key: String, event: String) {
        producer.send(ProducerRecord("teamdigihot.hm-soknadsbehandling-v1", key, event)).get(5, TimeUnit.SECONDS)
    }

    private fun shutdownHook() {
        Logg.info { "received shutdown signal, stopping app" }
        producer.close()
    }
}

private val jsonMapper: JsonMapper = jacksonMapperBuilder()
    .addModule(JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .build()

private fun createProducer(properties: Properties): Producer<String, String> {
    properties[ProducerConfig.ACKS_CONFIG] = "all"
    properties[ProducerConfig.LINGER_MS_CONFIG] = "0"
    properties[ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION] = "1"
    return KafkaProducer(properties, StringSerializer(), StringSerializer())
}
