package no.nav.hjelpemidler.delbestilling.rapportering

import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Tidspunktene som brukes her for kjøring av rapporter er noe vilkårlig valgt.
 * Men vi ønsker:
 * - å kjøre om natten
 * - litt offset mellom de ulike jobbene slik at de ikke går i beina på hverandre
 * - bruk sekund-presisjon på tidspunkt
 *      Med minutt-presisjon kan to noder schedulere med opptil 1 minutt
 *      forskjell (sekund=0 og sekund=59). Det kan skape krøll dersom lederen
 *      endrer seg mellom kjøringene til hver node.
 */

fun kl01NesteUkedag(clock: Clock): LocalDateTime {
    val nå = LocalDateTime.now(clock)
    var startTidspunkt = nå.withHour(1).withMinute(0).withSecond(0)

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

fun kl0120FørsteDagINesteMåned(clock: Clock): LocalDateTime {
    val nå = LocalDateTime.now(clock)
    var starttidspunkt = nå.withDayOfMonth(1).withHour(1).withMinute(20).withSecond(0)

    if (starttidspunkt < nå) {
        // Start neste måned, med mindre klokken nå er mellom 00:00 og 01:20 på den 1. i måneden
        starttidspunkt = starttidspunkt.plusMonths(1)
    }

    return starttidspunkt
}

fun kl0130FørsteDagINesteMåned(clock: Clock): LocalDateTime {
    val nå = LocalDateTime.now(clock)
    var starttidspunkt = nå.withDayOfMonth(1).withHour(1).withMinute(30).withSecond(0)

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