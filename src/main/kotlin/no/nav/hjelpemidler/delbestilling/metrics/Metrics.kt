package no.nav.hjelpemidler.delbestilling.metrics

import no.nav.hjelpemidler.delbestilling.delbestilling.Hmsnr
import no.nav.hjelpemidler.delbestilling.infrastructure.monitoring.logger2
import no.nav.hjelpemidler.delbestilling.kafka.KafkaService

private val log = logger2()

class Metrics(
    private val kafkaService: KafkaService,
) {
    private fun registerPoint(
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

    fun registrerDelbestillingInnsendt(
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

    fun grunndataHjelpemiddelManglerDeler(
        hmsnr: Hmsnr,
        navn: String,
    ) {
        registerPoint(
            "delbestilling.manglerDeler",
            mapOf(
                "hmsnr" to hmsnr,
                "navn" to navn,
            )
        )
    }

    fun antallKategorier(antallKategorier: Int) {
        registerPoint(
            "delbestilling.antallKategorier",
            mapOf(
                "antallKategorier" to antallKategorier.toString(),
            )
        )
    }
}
