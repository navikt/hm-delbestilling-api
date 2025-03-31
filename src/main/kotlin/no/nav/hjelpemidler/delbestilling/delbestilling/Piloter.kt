package no.nav.hjelpemidler.delbestilling.delbestilling

import no.nav.hjelpemidler.delbestilling.isDev

private const val kommunenummerOslo = "0301"

fun hentPiloter(brukersKommunenummer: String): List<Pilot> {
    val piloter = mutableListOf<Pilot>()
    if (brukersKommunenummer == kommunenummerOslo || isDev()) {
        piloter.add(Pilot.BESTILLE_IKKE_FASTE_LAGERVARER)
    }

    return piloter
}