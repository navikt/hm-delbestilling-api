package no.nav.hjelpemidler.delbestilling.hjelpemidler

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.delbestilling.Del
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.delerPerHjelpemiddel

private val logger = KotlinLogging.logger { }

object HjelpemiddelDeler {
    val hjelpemidler = lagHjelpemidler()

    fun hentHjelpemiddelMedDeler(hmsnrHjelpemiddel: String): HjelpemiddelMedDeler? {
        val hjelpemiddel = hjelpemidler.find { it.hmsnr == hmsnrHjelpemiddel } ?: return null
        logger.info { "Fant $hjelpemiddel for $hmsnrHjelpemiddel" }

        val deler = delerPerHjelpemiddel[hjelpemiddel.type] ?: emptyList()
        logger.info { "Fant ${deler.size} deler for $hmsnrHjelpemiddel" }

        return HjelpemiddelMedDeler(hjelpemiddel.navn, hjelpemiddel.hmsnr, deler, hjelpemiddel.type)
    }

    fun hentAlleHjelpemidlerMedDeler(): List<HjelpemiddelMedDeler> {
        return hjelpemidler.map {
            HjelpemiddelMedDeler(
                it.navn,
                it.hmsnr,
                delerPerHjelpemiddel[it.type] ?: emptyList(),
                it.type
            )
        }
    }

}

data class HjelpemiddelMedDeler(
    val navn: String,
    val hmsnr: String,
    val deler: List<Del>,
    val type: String,
)
