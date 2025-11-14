package no.nav.hjelpemidler.delbestilling.testdata

import no.nav.hjelpemidler.time.ZONE_ID_EUROPE_OSLO
import no.nav.hjelpemidler.time.toInstant
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class MutableClock(
    private var instant: Instant = Instant.now(),
    private var zone: ZoneId = ZONE_ID_EUROPE_OSLO
) : Clock() {

    constructor(dateTime: LocalDateTime) : this(dateTime.toInstant())

    override fun getZone(): ZoneId = zone

    override fun withZone(zone: ZoneId): Clock = MutableClock(instant, zone)

    override fun instant(): Instant = instant

    fun set(dateTime: LocalDateTime) {
        instant = dateTime.toInstant()
    }

    fun set(newInstant: Instant) {
        instant = newInstant
    }

    fun set(date: LocalDate) {
        instant = date.atStartOfDay().toInstant()
    }

}