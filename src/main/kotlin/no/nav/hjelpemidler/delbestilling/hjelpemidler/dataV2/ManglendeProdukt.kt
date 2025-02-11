package no.nav.hjelpemidler.delbestilling.hjelpemidler.dataV2

import no.nav.hjelpemidler.delbestilling.delbestilling.Hmsnr
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.Navn
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.alleProdukter
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

fun sjekkManglendeDeler(delKandidater: Map<Hmsnr, Navn>) {
    val eksisterendeDeler = hmsnrTilDel
    val nyeDeler = delKandidater.filter { it.key !in eksisterendeDeler }
    if (nyeDeler.isEmpty()) {
        return
    }

    println("Nye deler:")
    val now = LocalDate.now()
    nyeDeler.forEach {
        println(""" Del(hmsnr = "${it.key}", navn = "${it.value}", kategori = null, maksAntall = TODO_BESTEM_MAX_ANTALL, datoLagtTil = LocalDate.of(${now.year}, ${now.monthValue}, ${now.dayOfMonth})),""")
    }
}

private fun sjekkManglendeKoblinger(hjmTilDeler: Map<Hmsnr, Set<Hmsnr>>) {
    val eksisterendeKoblinger = hjmTilDeler
    val potensielleKoblinger =
        alleProdukter.flatMap { produkter -> produkter.hmsnr.map { hmsnr -> (hmsnr to produkter.deler.toSet()) } }
            .toMap()

    val nyeHjmKoblinger = potensielleKoblinger.filter { it.key !in eksisterendeKoblinger }
    if (nyeHjmKoblinger.isNotEmpty()) {
        println("Nye koblinger (hjm har ingen eksisterende kobling til deler):")
        nyeHjmKoblinger.forEach { printKobling(it.key, it.value) }
        println()
    }

    val eksisterendeKoblingerMedNyeDeler =
        potensielleKoblinger.filter {
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
    println("""  "$hmsnr" to setOf($delerString),""")
}

