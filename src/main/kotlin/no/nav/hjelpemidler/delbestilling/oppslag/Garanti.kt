package no.nav.hjelpemidler.delbestilling.oppslag

import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.LocalDate

private val log = KotlinLogging.logger { }

fun berikMedGaranti(
    hjelpemiddel: Hjelpemiddel,
    opprettetDato: LocalDate?,
    isokode: String?,
    artnr: String,
    identifikator: String,
): Hjelpemiddel {
    val garanti = garanti(
        opprettetDato = opprettetDato,
        isokode = isokode,
        artnr = artnr,
        identifikasjon = identifikator,
    ) ?: return hjelpemiddel

    return hjelpemiddel.copy(
        erInnenforGaranti = garanti.erInnenforGaranti(),
        antallÅrGaranti = garanti.antallÅr,
    )
}

private fun garanti(
    opprettetDato: LocalDate?,
    isokode: String?,
    artnr: String,
    identifikasjon: String,
): Garanti? {
    if (opprettetDato == null || isokode == null) {
        log.info { "Kan ikke beregne garantiperiode for utlån på artnr $artnr, $identifikasjon, fordi opprettetDato $opprettetDato eller isokode $isokode mangler." }
        return null
    }

    val antallÅrGaranti = beregnAntallÅrGaranti(isokode)

    return Garanti(
        start = opprettetDato,
        antallÅrGaranti,
    )
}

private fun beregnAntallÅrGaranti(isokode: String): Int {
    val isokodeERS = "1223"

    return when (isokode.take(4)) {
        isokodeERS -> 3
        else -> 2
    }
}

data class Garanti(
    val start: LocalDate,
    val antallÅr: Int
) {
    val slutt: LocalDate = start.plusYears(antallÅr.toLong())

    fun erInnenforGaranti(): Boolean {
        val nå = LocalDate.now()
        return start.isBefore(nå) && nå.isBefore(slutt)
    }
}
