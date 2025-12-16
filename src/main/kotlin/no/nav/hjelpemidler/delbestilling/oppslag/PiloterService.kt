package no.nav.hjelpemidler.delbestilling.oppslag

import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger { }

class PiloterService(
    // private val oebs: Oebs
) {

    suspend fun hentPiloter(brukersKommunenummer: String): List<Pilot> {
        /*
        Beholder denne snutten som utkommentert frem til man får bruk for den igjen i en fremtidig pilot.

        val brukersEnhet = oebs.finnLagerenhet(brukersKommunenummer)
        val piloter =  buildList {
            if (isDev() || PILOTENHETER_BESTILLE_IKKE_FASTE_LAGERVARER.contains(brukersEnhet)) {
                add(Pilot.BESTILLE_IKKE_FASTE_LAGERVARER)
            }
        }
         */

        log.info { "Ingen aktive piloter for øyeblikket. Returnerer tom pilotliste." }
        return emptyList()
    }
}
