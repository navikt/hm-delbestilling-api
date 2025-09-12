package no.nav.hjelpemidler.delbestilling.oppslag

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Utlån
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.tilOpprettetDato
import java.time.LocalDate

private val log = KotlinLogging.logger { }

fun berikMedGaranti(hjelpemiddel: Hjelpemiddel, utlån: Utlån, nå: LocalDate): Hjelpemiddel {
    if (utlån.opprettetDato == null || utlån.isokode == null) {
        log.info { "Utlån mangler opprettetDato eller isokode, returnerer hjelpemiddel" }
        return hjelpemiddel
    }

    val garantiPeriodeStart = utlån.opprettetDato.tilOpprettetDato() // I OeBS er opprettet dato det samme som garantiperiode-start
    val isokode = utlån.isokode.take(4)

    val antallÅrGaranti = when(isokode) {
        "1223" -> 3 // 1223 = Motordrevne rullestoler (ERS) har garantitid på 3 år
        else -> 2
    }

    val garantiPeriodeSlutt = garantiPeriodeStart.plusYears(antallÅrGaranti.toLong())
    val erInnenforGaranti = nå.isBefore(garantiPeriodeSlutt)

    return hjelpemiddel.copy(erInnenforGaranti = erInnenforGaranti, antallÅrGaranti = antallÅrGaranti)
}