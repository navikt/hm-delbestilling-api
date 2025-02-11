package no.nav.hjelpemidler.delbestilling.hjelpemidler.data

import no.nav.hjelpemidler.delbestilling.delbestilling.Del
import no.nav.hjelpemidler.delbestilling.delbestilling.HjelpemiddelMedDeler
import no.nav.hjelpemidler.delbestilling.delbestilling.Hmsnr
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hjelpemidler.AZALEA
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hjelpemidler.C500
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hjelpemidler.COMET
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hjelpemidler.ELOFLEX
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hjelpemidler.F3F5M3M5
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hjelpemidler.MC11xx
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hjelpemidler.MINICROSSER
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hjelpemidler.MOLIFT
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hjelpemidler.OPUS
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hjelpemidler.ORION
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hjelpemidler.PANTHERA
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hjelpemidler.X850


typealias Navn = String

data class HjelpemidlerOgDeler(
    val navn: Navn,
    val deler: List<Hmsnr>,
    val hmsnr: List<Hmsnr>,
)

data class Hjelpemiddel(
    val navn: Navn,
    val hmsnr: Hmsnr
)

data class DelMedHjelpemidler(
    val del: Del,
    val hjelpemidler: List<Hjelpemiddel>
)

val alleProdukter =
    listOf(AZALEA, C500, COMET, ELOFLEX, F3F5M3M5, MC11xx, MINICROSSER, MOLIFT, OPUS, ORION, PANTHERA, X850).flatten()

val hmsnr2Hjm: Map<Hmsnr, HjelpemiddelMedDeler> = alleProdukter
    .flatMap { hjelpemiddlerOgDeler ->
        hjelpemiddlerOgDeler.hmsnr.map { hmsnr ->
            val deler = hjelpemiddlerOgDeler.deler.map {
                alleDeler[it] ?: throw IllegalArgumentException("Fant ingen del med hmsnr $it")
            }
            hmsnr to HjelpemiddelMedDeler(
                navn = hjelpemiddlerOgDeler.navn,
                hmsnr = hmsnr,
                deler = deler,
            )
        }
    }.toMap()

val hmsnr2Del: Map<Hmsnr, DelMedHjelpemidler> = alleDeler.mapValues { (hmsnr, del) ->
    val hjmHmsnr = mutableSetOf<Hmsnr>()
    alleProdukter.forEach {
        if (it.deler.contains(hmsnr)) {
            hjmHmsnr.addAll(it.hmsnr)
        }
    }
    val hjelpemidler = hjmHmsnr.map {
        Hjelpemiddel(
            navn = hmsnr2Hjm[it]?.navn ?: throw IllegalArgumentException("Mangler navn for hjelpemiddel $it"),
            hmsnr = it
        )
    }
    if (hjelpemidler.isEmpty()) {
        throw IllegalStateException("Mangler hjelpemidler for del $del")
    }
    DelMedHjelpemidler(
        del = del,
        hjelpemidler = hjelpemidler
    )
}