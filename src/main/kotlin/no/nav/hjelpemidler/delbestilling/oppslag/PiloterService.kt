package no.nav.hjelpemidler.delbestilling.oppslag

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.common.Lager
import no.nav.hjelpemidler.delbestilling.config.isDev
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Oebs

private val log = KotlinLogging.logger { }

private val PILOTENHETER_BESTILLE_IKKE_FASTE_LAGERVARER = setOf(
    Lager.OSLO,
    Lager.ØST_VIKEN,
    Lager.INNLANDET_ELVERUM,
    Lager.VESTFOLD_OG_TELEMARK,
    Lager.VEST_VIKEN,
    Lager.MØRE_OG_ROMSDAL,
    Lager.VESTLAND_BERGEN,
    // Lager.TRØNDELAG, // Skrur av for Trøndelag i påvente av fiks for to lager
    Lager.ROGALAND,
    Lager.AGDER,
    Lager.VESTLAND_FØRDE,
    Lager.NORDLAND,
    Lager.INNLANDET_GJØVIK,
    Lager.TROMS,
    Lager.FINNMARK,
)

class PiloterService(
    private val oebs: Oebs
) {

    suspend fun hentPiloter(brukersKommunenummer: String): List<Pilot> {
        val brukersEnhet = oebs.finnLagerenhet(brukersKommunenummer)

        val piloter =  buildList {
            if (isDev() || PILOTENHETER_BESTILLE_IKKE_FASTE_LAGERVARER.contains(brukersEnhet)) {
                add(Pilot.BESTILLE_IKKE_FASTE_LAGERVARER)
            }
        }
        log.info { "Fant enhet=$brukersEnhet for kommunenummer=$brukersKommunenummer i pilotsjekk, med piloter=$piloter" }

        return piloter
    }
}
