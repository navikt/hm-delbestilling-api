package no.nav.hjelpemidler.delbestilling.metrics

import mu.KotlinLogging
import no.nav.hjelpemidler.delbestilling.kafka.KafkaService

private val log = KotlinLogging.logger {}

class Metrics(
    private val kafkaService: KafkaService,
) {
    private suspend fun registerPoint(
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

    suspend fun registrerDelbestillingInnsendt(
        hmsnrDel: String,
        navnDel: String,
        hmsnrHovedprodukt: String,
        navnHovedprodukt: String,
        rolleInnsender: String,
        hjmbrukerHarBrukerpass: Boolean
    ) {
        registerPoint(
            "delbestilling.innsendt",
            mapOf(
                "hmsnrDel" to hmsnrDel,
                "navnDel" to navnDel,
                "hmsnrHovedprodukt" to hmsnrHovedprodukt,
                "navnHovedprodukt" to navnHovedprodukt,
                "rolleInnsender" to rolleInnsender,
                "hjmbrukerHarBrukerpass" to hjmbrukerHarBrukerpass.toString(),
            )
        )
    }
}
