package no.nav.hjelpemidler.delbestilling.rapportering

import java.time.Clock
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

fun kl01NesteUkedag(clock: Clock): LocalDateTime {
    val nå = LocalDateTime.now(clock)
    var startTidspunkt = nå.withHour(1).withMinute(0).withSecond(0).withNano(0)

    if (startTidspunkt <= nå) {
        // Start neste dag, med mindre klokken nå er mellom 00:00 og 00:59
        startTidspunkt = startTidspunkt.plusDays(1)
    }

    while (erHelg(startTidspunkt.toLocalDate())) {
        // Hopp over helgedager
        startTidspunkt = startTidspunkt.plusDays(1)
    }

    return startTidspunkt
}

fun kl01FørsteDagINesteMåned(clock: Clock): LocalDateTime {
    val nå = LocalDateTime.now(clock)
    var starttidspunkt = nå.withDayOfMonth(1).withHour(1).withMinute(0).withSecond(0).withNano(0)

    if (starttidspunkt < nå) {
        // Start neste måned, med mindre klokken nå er mellom 00:00 og 00:59 på den 1. i måneden
        starttidspunkt = starttidspunkt.plusMonths(1)
    }

    return starttidspunkt
}

private fun erHelg(dato: LocalDate): Boolean {
    val dag = dato.dayOfWeek
    return dag == DayOfWeek.SATURDAY || dag == DayOfWeek.SUNDAY
}