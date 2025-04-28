package no.nav.hjelpemidler.delbestilling.delbestilling
import no.nav.hjelpemidler.delbestilling.delbestilling.model.Pilot
import no.nav.hjelpemidler.delbestilling.config.isDev
import no.nav.hjelpemidler.hjelpemidlerdigitalSoknadapi.tjenester.norg.NorgService

private val PILOTENHETER_BESTILLE_IKKE_FASTE_LAGERVARER = listOf(ENHETNR_OSLO, ENHETNR_ØST_VIKEN)

class PiloterService(
    private val norgService: NorgService
) {
    suspend fun hentPiloter(brukersKommunenummer: String): List<Pilot> {
        val brukersEnhetnr = norgService.hentArbeidsfordelingenhet(brukersKommunenummer).enhetNr

        val piloter = mutableListOf<Pilot>()

        if (isDev() || PILOTENHETER_BESTILLE_IKKE_FASTE_LAGERVARER.contains(brukersEnhetnr)) {
            piloter.add(Pilot.BESTILLE_IKKE_FASTE_LAGERVARER)
        }

        return piloter
    }
}
