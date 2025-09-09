package no.nav.hjelpemidler.delbestilling.oppslag

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.database.transaction
import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private val log = KotlinLogging.logger { }

suspend fun berikMedGaranti(hjelpemiddel: Hjelpemiddel, opprettetDato: LocalDate): Hjelpemiddel {
    val garantiPeriodeStart = opprettetDato // I OeBS er opprettet dato det samme som garantiperiode-start
    val garantiPeriodeSlutt = garantiPeriodeStart.plusYears(2)
    // TODO: Støtte garantiperiode for ERS
    return hjelpemiddel.copy(erInnenforGaranti = LocalDate.now().isBefore(garantiPeriodeSlutt))
}