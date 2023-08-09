package no.nav.hjelpemidler.delbestilling.metrics

import mu.KotlinLogging
import no.nav.hjelpemidler.delbestilling.kafka.KafkaService

private val log = KotlinLogging.logger {}

class Metrics(
    private val kafkaService: KafkaService,
) {
    suspend fun registerPoint(
        measurement: String,
        tags: Map<String, String>,
        fields: Map<String, Any> = mapOf("counter" to 1L),
    ) {
        try {
            kafkaService.hendelseOpprettet(measurement, fields, tags)
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
