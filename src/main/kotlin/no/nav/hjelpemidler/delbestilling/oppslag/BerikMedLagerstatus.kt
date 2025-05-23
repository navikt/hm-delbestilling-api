package no.nav.hjelpemidler.delbestilling.oppslag

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.infrastructure.metrics.Metrics
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Oebs


private val log = KotlinLogging.logger {}

class BerikMedLagerstatus(
    private val oebs: Oebs,
    private val metrics: Metrics,
) {

    suspend operator fun invoke(hjelpemiddel: Hjelpemiddel, kommunenummer: String): Hjelpemiddel {
        val lagerstatusForDeler = oebs.hentLagerstatusForKommunenummerAsMap(kommunenummer, hjelpemiddel.delerHmsnr())

        val beriket = hjelpemiddel.medLagerstatus(lagerstatusForDeler)

        loggOgSendStatistikk(beriket)

        return beriket
    }

    private fun loggOgSendStatistikk(hjelpemiddel: Hjelpemiddel) {
        val hmsnr = hjelpemiddel.hmsnr
        val sentral = hjelpemiddel.deler.first().lagerstatus?.organisasjons_navn ?: "UKJENT"
        val antallPåLager = hjelpemiddel.deler.count { it.lagerstatus?.minmax == true }
        val antallDeler = hjelpemiddel.deler.count()
        log.info { "Lagerstatus for $hmsnr hos $sentral: $antallPåLager av $antallDeler er på lager." }
        if (antallPåLager < antallDeler) {
            val ikkePåLager = hjelpemiddel.deler.filter { it.lagerstatus?.minmax == false }.map { it.hmsnr }
            val manglerLagerstatus = hjelpemiddel.deler.filter { it.lagerstatus == null }.map { it.hmsnr }
            log.info { "$sentral har ikke alle deler på lager for $hmsnr. Ikke på lager: $ikkePåLager, mangler lagerstatus: $manglerLagerstatus." }
        }

        log.info { "Antall deler for hmsnr $hmsnr: ${hjelpemiddel.deler.size}, antall unike kategorier: ${hjelpemiddel.antallKategorier}" }
        metrics.antallKategorier(hjelpemiddel.antallKategorier)

        val antallDelerTilgjengeligMenIkkePåMinmax =
            hjelpemiddel.deler.count { it.lagerstatus != null && !it.lagerstatus!!.minmax && it.lagerstatus!!.tilgjengelig > 0 }
        if (antallDelerTilgjengeligMenIkkePåMinmax > 0) {
            log.info { "Antall tilgjengelig deler (ikke minmax) for $hmsnr: $antallDelerTilgjengeligMenIkkePåMinmax" }
        }
    }
}