package no.nav.hjelpemidler.delbestilling.hjelpemidler.data

import no.nav.hjelpemidler.delbestilling.delbestilling.Hmsnr
import no.nav.hjelpemidler.delbestilling.hjelpemidler.Navn
import java.time.LocalDate

/**
 * Funksjon som leser Excel-filer i resources,
 * parser innholdet til hjelpemidler og deler,
 * sammenligner med innholdet i dagens sortiment,
 * printer ut oversikt over produkt og koblinger som mangler i dagens sortiment.
 *
 * OBS: Sjekker kun hva som mangler, ikke hva som potensielt bør fjernes.
 */
fun main() {
    println("Sjekker hvilke produkter og koblinger som mangler i sortimentet...")

    val data = Xlsx().parse()

    sjakkManglendeHjelpemidler(data.hjelpemidler)
    sjekkManglendeDeler(data.deler)
    sjekkManglendeKoblinger(data.hjmTilDeler)

    val antallEksisterendeDeler = hmsnrTilDel.size
    val antallNyeDeler = data.deler.size
    val økningDeler = ((((antallEksisterendeDeler + antallNyeDeler).toDouble() / antallEksisterendeDeler)-1) * 100).toInt()
    println("Antall nye deler: $antallNyeDeler. Antall eksisterende deler: $antallEksisterendeDeler. Økning: $økningDeler%")

    val antallEksisterendeHjm = hmsnrTilHjelpemiddel.size
    val antallNyeHjm = data.hjelpemidler.size
    val økningHjm = ((((antallEksisterendeHjm + antallNyeHjm).toDouble() / antallEksisterendeHjm)-1) * 100).toInt()
    println("Antall nye hjm: $antallNyeHjm. Antall eksisterende hjm: $antallEksisterendeHjm. Økning: $økningHjm%")

    println("Sjekk fullført!")
}

fun sjakkManglendeHjelpemidler(hjelpemiddelKandidater: Map<Hmsnr, Navn>) {
    val eksisterendeHjelpemidler = hmsnrTilHjelpemiddel
    val nyeHjelpemidler = hjelpemiddelKandidater.filter { it.key !in eksisterendeHjelpemidler }
    if (nyeHjelpemidler.isEmpty()) {
        return
    }

    println("Nye hjelpemidler:")
    nyeHjelpemidler.forEach {
        println(""" Hjelpemiddel(hmsnr = "${it.key}", navn = "${it.value}"),""")
    }
}

fun sjekkManglendeDeler(delKandidater: Map<Hmsnr, ParsedDel>) {
    val eksisterendeDeler = hmsnrTilDel
    val nyeDeler = delKandidater.filter { it.key !in eksisterendeDeler }
    if (nyeDeler.isEmpty()) {
        return
    }

    println("Nye deler:")
    val now = LocalDate.now()
    nyeDeler.forEach {
        val levartnr = if (it.value.levArtNr.isNullOrBlank()) "" else """, levArtNr = ${it.value.levArtNr}"""
        println(""" Del(hmsnr = "${it.key}", navn = "${it.value.navn}"$levartnr, kategori = null, defaultAntall = null, maksAntall = null, datoLagtTil = LocalDate.of(${now.year}, ${now.monthValue}, ${now.dayOfMonth})),""")
    }
}

private fun sjekkManglendeKoblinger(koblingKandidater: Map<Hmsnr, Set<Hmsnr>>) {
    val eksisterendeKoblinger = hmsnrHjmTilHmsnrDeler

    val nyeHjmKoblinger = koblingKandidater.filter { it.key !in eksisterendeKoblinger }
    if (nyeHjmKoblinger.isNotEmpty()) {
        println("Nye koblinger (hjm har ingen eksisterende kobling til deler):")
        nyeHjmKoblinger.forEach { printKobling(it.key, it.value) }
        println()
    }

    val eksisterendeKoblingerMedNyeDeler =
        koblingKandidater.filter {
            it.key in eksisterendeKoblinger &&
                    it.value.union(eksisterendeKoblinger[it.key]!!).size > eksisterendeKoblinger[it.key]!!.size
        }
    if (eksisterendeKoblingerMedNyeDeler.isNotEmpty()) {
        println("Nye deler på eksisterende kobling:")
        eksisterendeKoblingerMedNyeDeler.forEach { printKobling(it.key, it.value) }
        println()
    }
}

private fun printKobling(hmsnr: Hmsnr, deler: Set<Hmsnr>) {
    val delerString = deler.joinToString(prefix = "\"", separator = "\", \"", postfix = "\"")
    println(""" "$hmsnr" to setOf($delerString),""")
}

