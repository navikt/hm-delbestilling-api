package no.nav.hjelpemidler.delbestilling.oppslag

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Utlån
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.tilOpprettetDato
import java.time.LocalDate

private val log = KotlinLogging.logger { }

suspend fun berikMedGaranti(hjelpemiddel: Hjelpemiddel, utlån: Utlån): Hjelpemiddel {
    val garantiPeriodeStart = utlån.opprettetDato.tilOpprettetDato() // I OeBS er opprettet dato det samme som garantiperiode-start

    val antallÅrGaranti = when(utlån.isokode.take(4)) {
        "1223" -> 3 // 1223 = Motordrevne rullestoler (ERS) har garantitid på 3 år
        else -> 2
    }.toLong()

    val garantiPeriodeSlutt = garantiPeriodeStart.plusYears(2)
    // TODO: Støtte garantiperiode for ERS
    return hjelpemiddel.copy(erInnenforGaranti = LocalDate.now().isBefore(garantiPeriodeSlutt))
}