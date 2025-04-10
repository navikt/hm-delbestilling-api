package no.nav.hjelpemidler.delbestilling.delbestilling
import no.nav.hjelpemidler.delbestilling.delbestilling.model.Pilot
import no.nav.hjelpemidler.delbestilling.isDev
import no.nav.hjelpemidler.hjelpemidlerdigitalSoknadapi.tjenester.norg.NorgService

private val ENHETNR_OSLO = "4703"
private val PILOTENHETER_BESTILLE_IKKE_FASTE_LAGERVARER = listOf(ENHETNR_OSLO)

class PiloterService(
    private val norgService: NorgService
) {
    suspend fun hentPiloter(brukersKommunenummer: String): List<Pilot>  {
        val brukersEnhetnr = norgService.hentHmsEnhet(brukersKommunenummer).enhetNr

        val piloter = mutableListOf<Pilot>()

        if (isDev() || PILOTENHETER_BESTILLE_IKKE_FASTE_LAGERVARER.contains(brukersEnhetnr)) {
            piloter.add(Pilot.BESTILLE_IKKE_FASTE_LAGERVARER)
        }

        return piloter
    }
}

