package no.nav.hjelpemidler.delbestilling.rapportering

import java.time.Clock
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.days

val ETT_DØGN = 1.days.inWholeMilliseconds

fun erHelg(clock: Clock): Boolean {
    val iDag = LocalDate.now(clock).dayOfWeek
    return iDag == DayOfWeek.SATURDAY || iDag == DayOfWeek.SUNDAY
}

fun delayTilKl01(clock: Clock): Long {
    val nå = LocalDateTime.now(clock)

    var startTidspunkt = nå.withHour(1).withMinute(0).withSecond(0).withNano(0)
    if (startTidspunkt <= nå) {
        startTidspunkt = startTidspunkt.plusDays(1)
    }

    return Duration.between(nå, startTidspunkt).toMillis()
}