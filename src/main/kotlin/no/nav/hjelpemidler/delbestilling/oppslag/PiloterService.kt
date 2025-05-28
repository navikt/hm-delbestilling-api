package no.nav.hjelpemidler.delbestilling.oppslag

import no.nav.hjelpemidler.delbestilling.common.Enhet
import no.nav.hjelpemidler.delbestilling.config.isDev
import no.nav.hjelpemidler.hjelpemidlerdigitalSoknadapi.tjenester.norg.Norg

private val PILOTENHETER_BESTILLE_IKKE_FASTE_LAGERVARER = setOf(
    Enhet.OSLO,
    Enhet.ØST_VIKEN,
    Enhet.INNLANDET_ELVERUM,
    Enhet.VESTFOLD_OG_TELEMARK,
    Enhet.VEST_VIKEN,
    Enhet.MØRE_OG_ROMSDAL,
    Enhet.VESTLAND_BERGEN,
    Enhet.TRØNDELAG,
).map { it.nummer }

class PiloterService(
    private val norg: Norg
) {

    suspend fun hentPiloter(brukersKommunenummer: String): List<Pilot> {
        val brukersEnhet = norg.hentEnhetnummer(brukersKommunenummer)

        return buildList {
            if (isDev() || PILOTENHETER_BESTILLE_IKKE_FASTE_LAGERVARER.contains(brukersEnhet)) {
                add(Pilot.BESTILLE_IKKE_FASTE_LAGERVARER)
            }
        }
    }
}
