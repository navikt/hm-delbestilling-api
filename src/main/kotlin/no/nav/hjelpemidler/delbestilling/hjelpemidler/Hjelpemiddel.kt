package no.nav.hjelpemidler.delbestilling.hjelpemidler

import mu.KotlinLogging
import no.nav.hjelpemidler.delbestilling.delbestilling.Hjelpemiddel

private val logger = KotlinLogging.logger { }

fun lagHjelpemidler(): List<Hjelpemiddel> {
    val linjer = object {}.javaClass.getResourceAsStream("/hjelpemidler.txt")!!.bufferedReader().readLines()
    val hjelpemiddelTyper = listOf("Azalea", "Comet", "Cross", "Minicrosser", "Netti", "Panthera", "X850", "X850S")
    val hjelpemiddelTyperLowercase = hjelpemiddelTyper.map { it.lowercase() }

    val hjelpemidler = linjer.map { linje ->
        val (hmsnr, navn) = linje.split(" ", limit = 2)
        val navnTokens = navn.lowercase().split(" ").toSet()
        val typeIndex = hjelpemiddelTyperLowercase.indexOfFirst { navnTokens.contains(it) }

        if (typeIndex == -1) {
            logger.error { "Fant ikke gyldig type i $navn" }
            return@map null
        }

        val type = hjelpemiddelTyper[typeIndex]

        Hjelpemiddel(hmsnr, navn, type)
    }.filterNotNull()

    logger.info { "Parset ut ${hjelpemidler.size} hjelpemidler" }

    return hjelpemidler
}