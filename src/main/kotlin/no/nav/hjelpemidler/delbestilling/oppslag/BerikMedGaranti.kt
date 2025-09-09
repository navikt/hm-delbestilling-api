package no.nav.hjelpemidler.delbestilling.oppslag

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Utlån
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.tilOpprettetDato
import java.time.LocalDate

private val log = KotlinLogging.logger { }

suspend fun berikMedGaranti(hjelpemiddel: Hjelpemiddel, utlån: Utlån): Hjelpemiddel {
    val garantiPeriodeStart = utlån.opprettetDato.tilOpprettetDato() // I OeBS er opprettet dato det samme som garantiperiode-start

    val isokode = utlån.isokode.take(4)

    val antallÅrGaranti = when(isokode) {
        "1223" -> 3 // 1223 = Motordrevne rullestoler (ERS) har garantitid på 3 år
        else -> 2
    }.toLong()

    log.info { "antallÅrGaranti for $isokode: $antallÅrGaranti" }

    val garantiPeriodeSlutt = garantiPeriodeStart.plusYears(antallÅrGaranti)
    val nå = LocalDate.now()
    val erInnenforGaranti = nå.isBefore(garantiPeriodeSlutt)
    return hjelpemiddel.copy(erInnenforGaranti = erInnenforGaranti)
}