package no.nav.hjelpemidler.delbestilling.metrics

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.delbestilling.model.DelLinje
import no.nav.hjelpemidler.delbestilling.delbestilling.model.DelbestillingSak
import no.nav.hjelpemidler.delbestilling.delbestilling.model.Hmsnr
import no.nav.hjelpemidler.delbestilling.kafka.KafkaService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

private val log = KotlinLogging.logger {}

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

    fun delSkipningsbekreftet(sak: DelbestillingSak, dellinje: DelLinje, skipningsbekreftet: LocalDate) {
        val lagerstatus = dellinje.lagerstatusPåBestillingstidspunkt
        val lagerstatusType = when {
            lagerstatus == null -> return // Bakoverkompabilitet
            lagerstatus.antallDelerPåLager <= 0 -> "IKKE_PÅ_LAGER"
            lagerstatus.antallDelerPåLager < dellinje.antall -> "DELVIS_PÅ_LAGER"
            else -> "PÅ_LAGER"
        }
        registerPoint(
            "delbestilling.delSkipningsbekreftet",
            mapOf(
                "hmsnr" to dellinje.del.hmsnr,
                "dagerTilSkipningsbekreftelse" to dagerMellom(sak.opprettet, skipningsbekreftet).toString(),
                "lagerstatusVedBestilling" to lagerstatusType,
                "minmax" to lagerstatus.minmax.toString(),
            )
        )
    }

}

private fun dagerMellom(fra: LocalDateTime, til: LocalDate) = ChronoUnit.DAYS.between(fra.toLocalDate(), til)

