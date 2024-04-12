package no.nav.hjelpemidler.delbestilling.hjelpemidler.data

import no.nav.hjelpemidler.delbestilling.delbestilling.Del
import no.nav.hjelpemidler.delbestilling.delbestilling.HjelpemiddelMedDeler
import no.nav.hjelpemidler.delbestilling.delbestilling.Hmsnr
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hjelpemidler.AZALEA
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hjelpemidler.C500
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hjelpemidler.COMET
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hjelpemidler.ELOFLEX
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hjelpemidler.MINICROSSER
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hjelpemidler.MOLIFT
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hjelpemidler.OPUS
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hjelpemidler.ORION
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hjelpemidler.PANTHERA
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hjelpemidler.X850


typealias Navn = String

data class HjelpemidlerOgDeler(
    val navn: Navn,
    val type: Navn,
    val deler: List<Hmsnr>,
    val hmsnr: List<Hmsnr>,
)

private val alleProdukter =
    listOf(AZALEA, C500, COMET, ELOFLEX, MINICROSSER, MOLIFT, OPUS, ORION, PANTHERA, X850).flatten()

private val hjmNavn2HjelpemiddelOgDeler: Map<Navn, HjelpemidlerOgDeler> = alleProdukter
    .associateBy { it.navn }

val hjmHmsnr2HjelpemiddelMedDeler: Map<Hmsnr, HjelpemiddelMedDeler> =
    hjmNavn2HjelpemiddelOgDeler.values.flatMap { hjelpemiddlerOgDeler ->
        hjelpemiddlerOgDeler.hmsnr.map { hmsnr ->
            val deler = hjelpemiddlerOgDeler.deler.map {
                DELER[it] ?: throw IllegalArgumentException("Fant ingen del med hmsnr $it")
            }
            hmsnr to HjelpemiddelMedDeler(
                navn = hjelpemiddlerOgDeler.navn,
                type = hjelpemiddlerOgDeler.type,
                hmsnr = hmsnr,
                deler = deler,
            )
        }
    }.toMap()

val hjmNavn2Deler: Map<Navn, List<Del>> =
    hjmNavn2HjelpemiddelOgDeler.mapValues {
        it.value.deler.map { hmsnr ->
            DELER[hmsnr] ?: throw IllegalArgumentException("Fant ingen del med hmsnr $hmsnr")
        }
    }



private fun finnHjmType2Deler() : Map<Navn, List<Del>> {
    val tmp = mutableMapOf<Navn, MutableSet<Hmsnr>>()
    alleProdukter.forEach { produkt ->
        if (produkt.type !in tmp) {
            tmp[produkt.type] = mutableSetOf()
        }
        tmp[produkt.type]!!.addAll(produkt.deler)
    }
    return tmp.mapValues { (type, deler) -> deler.map { delHmsnr -> DELER[delHmsnr]!! } }
}

val hjmType2Deler = finnHjmType2Deler()