package no.nav.hjelpemidler.delbestilling.oppslag

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Utlån
import java.time.LocalDate

private val log = KotlinLogging.logger { }

fun Utlån.garanti(): Garanti? {
    if (opprettetDato == null || isokode == null) {
        log.info { "Kan ikke beregne garantiperiode for utlån på artnr $artnr, serienr $serienr, fordi opprettetDato $opprettetDato eller isokode $isokode mangler." }
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