package no.nav.hjelpemidler.delbestilling.oppslag

import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger { }

class PiloterService {

    fun hentPiloter(): List<Pilot> {
        log.info { "Ingen aktive piloter for øyeblikket. Returnerer tom pilotliste." }
        return emptyList()
    }
}
