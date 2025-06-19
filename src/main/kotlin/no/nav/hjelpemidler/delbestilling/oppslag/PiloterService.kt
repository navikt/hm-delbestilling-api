package no.nav.hjelpemidler.delbestilling.oppslag

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.common.Enhet
import no.nav.hjelpemidler.delbestilling.config.isDev
import no.nav.hjelpemidler.delbestilling.infrastructure.norg.Norg

private val log = KotlinLogging.logger { }

private val PILOTENHETER_BESTILLE_IKKE_FASTE_LAGERVARER = setOf(
    Enhet.OSLO,
    Enhet.ØST_VIKEN,
    Enhet.INNLANDET_ELVERUM,
    Enhet.VESTFOLD_OG_TELEMARK,
    Enhet.VEST_VIKEN,
    Enhet.MØRE_OG_ROMSDAL,
    Enhet.VESTLAND_BERGEN,
    Enhet.TRØNDELAG,
    Enhet.ROGALAND,
    Enhet.AGDER,
    Enhet.VESTLAND_FØRDE,
    Enhet.NORDLAND,
    Enhet.INNLANDET_GJØVIK,
)

class PiloterService(
    private val norg: Norg
) {

    suspend fun hentPiloter(brukersKommunenummer: String): List<Pilot> {
        val brukersEnhet = norg.hentEnhet(brukersKommunenummer)

        val piloter =  buildList {
            if (isDev() || PILOTENHETER_BESTILLE_IKKE_FASTE_LAGERVARER.contains(brukersEnhet)) {
                add(Pilot.BESTILLE_IKKE_FASTE_LAGERVARER)
            }
        }
        log.info { "Fant enhet=$brukersEnhet for kommunenummer=$brukersKommunenummer i pilotsjekk, med piloter=$piloter" }

        return piloter
    }
}
