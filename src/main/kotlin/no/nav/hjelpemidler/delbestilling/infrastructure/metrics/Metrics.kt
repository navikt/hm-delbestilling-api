package no.nav.hjelpemidler.delbestilling.infrastructure.metrics

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.common.DelbestillingSak
import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.infrastructure.kafka.Kafka
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

private val log = KotlinLogging.logger {}

class Metrics(
    private val kafka: Kafka,
) {

    private inline fun registerSafely(measurement: String, tagsProvider: () -> Map<String, String>) {
        try {
            val tags = tagsProvider()
            val fields = mapOf("counter" to 1L)
            kafka.hendelseOpprettet(measurement, fields, tags)
        } catch (e: Exception) {
            log.error(e) { "Feil under metrikk-prosessering for '$measurement'" }
        }
    }

    fun registrerDelbestillingInnsendt(
        hmsnrDel: String,
        navnDel: String,
        hmsnrHovedprodukt: String,
        navnHovedprodukt: String,
        rolleInnsender: String,
        hjmbrukerHarBrukerpass: Boolean
    ) = registerSafely("delbestilling.innsendt") {
        mapOf(
            "hmsnrDel" to hmsnrDel,
            "navnDel" to navnDel,
            "hmsnrHovedprodukt" to hmsnrHovedprodukt,
            "navnHovedprodukt" to navnHovedprodukt,
            "rolleInnsender" to rolleInnsender,
            "hjmbrukerHarBrukerpass" to hjmbrukerHarBrukerpass.toString(),
        )
    }

    fun grunndataHjelpemiddelManglerDeler(hmsnr: Hmsnr, navn: String) =
        registerSafely("delbestilling.manglerDeler") {
            mapOf(
                "hmsnr" to hmsnr,
                "navn" to navn,
            )
        }

    fun antallKategorier(antallKategorier: Int) =
        registerSafely("delbestilling.antallKategorier") {
            mapOf(
                "antallKategorier" to antallKategorier.toString(),
            )
        }

    fun delSkipningsbekreftet(sak: DelbestillingSak, hmsnr: Hmsnr, skipningsbekreftet: LocalDate) =
        registerSafely("delbestilling.delSkipningsbekreftet") {
            val dellinje = sak.delbestilling.deler.find { it.del.hmsnr == hmsnr } ?: return
            val lagerstatus = dellinje.lagerstatusPåBestillingstidspunkt
            val lagerstatusType = when {
                lagerstatus == null -> return // Bakoverkompabilitet
                lagerstatus.antallDelerPåLager <= 0 -> "IKKE_PÅ_LAGER"
                lagerstatus.antallDelerPåLager < dellinje.antall -> "DELVIS_PÅ_LAGER"
                else -> "PÅ_LAGER"
            }
            mapOf(
                "hmsnr" to dellinje.del.hmsnr,
                "dagerTilSkipningsbekreftelse" to dagerMellom(sak.opprettet, skipningsbekreftet).toString(),
                "lagerstatusVedBestilling" to lagerstatusType,
                "minmax" to lagerstatus.minmax.toString(),
            )
        }
}

private fun dagerMellom(fra: LocalDateTime, til: LocalDate) = ChronoUnit.DAYS.between(fra.toLocalDate(), til)

