package no.nav.hjelpemidler.delbestilling.hjelpemidler

import mu.KotlinLogging
import no.nav.hjelpemidler.delbestilling.delbestilling.Hjelpemiddel

private val logger = KotlinLogging.logger { }

fun main() {
    val hjelpemidler = lagHjelpemidler()

    println(hjelpemidler)
}

fun lagHjelpemidler(): List<Hjelpemiddel> {
    val linjer = object {}.javaClass.getResourceAsStream("/hjelpemidler.txt")!!.bufferedReader().readLines()

    val hjelpemidler = linjer.map { linje ->
        val (hmsnr, navn, type) = linje.split("  ")
        Hjelpemiddel(hmsnr, navn, type)
    }

    return hjelpemidler
}
