package no.nav.hjelpemidler.delbestilling.metrics

import mu.KotlinLogging
import no.nav.hjelpemidler.delbestilling.kafka.KafkaProducer

private val log = KotlinLogging.logger {}

class Metrics(
    private val kafkaProducer: KafkaProducer,
) {
    suspend fun registerPoint(
        measurement: String,
        tags: Map<String, String>,
        fields: Map<String, Any> = mapOf("counter" to 1L),
    ) {

        try {
            kafkaProducer.hendelseOpprettet(measurement, fields, tags)
        } catch (e: Exception) {
            log.error(e) { "Feil under registrering av metric <$measurement>" }
        }
    }

    private suspend fun registrerSomething() {
        registerPoint(
            SOMETHING,
            mapOf(
                "rolle" to "tekniker",
                "antallDeler" to "5",
            )
        )
    }
}

val SOMETHING = "something"
