package no.nav.hjelpemidler.delbestilling.oppslag

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.database.transaction
import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction.Transactional
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private val log = KotlinLogging.logger { }

suspend fun berikMedGaranti(hjelpemiddel: Hjelpemiddel, garantidato: LocalDate): Hjelpemiddel {
    val garantiPeriodeSlutt = garantidato.plusYears(2)
    // TODO: Støtte garantiperiode for ERS
    return hjelpemiddel.copy(erInnenforGaranti = LocalDate.now().isBefore(garantiPeriodeSlutt))
}