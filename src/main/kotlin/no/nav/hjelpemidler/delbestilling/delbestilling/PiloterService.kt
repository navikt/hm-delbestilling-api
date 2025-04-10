package no.nav.hjelpemidler.delbestilling.delbestilling

import com.github.benmanes.caffeine.cache.Caffeine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import no.nav.hjelpemidler.delbestilling.delbestilling.model.Pilot
import no.nav.hjelpemidler.delbestilling.isDev
import no.nav.hjelpemidler.hjelpemidlerdigitalSoknadapi.tjenester.norg.NorgService
import java.util.concurrent.TimeUnit

private val ENHETNR_OSLO = "4703"
private val PILOTENHETER_BESTILLE_IKKE_FASTE_LAGERVARER = listOf(ENHETNR_OSLO)

class PiloterService(
    private val norgService: NorgService
) {
    private val cache = Caffeine.newBuilder()
        .expireAfterWrite(7, TimeUnit.DAYS)
        .maximumSize(400)
        .build<String, Deferred<List<Pilot>>>()

    suspend fun hentPiloter(brukersKommunenummer: String): List<Pilot> = cache.get("piloter_for_kommunenr_$brukersKommunenummer") {
        CoroutineScope(Dispatchers.IO).async {
            val brukersEnhetnr = norgService.hentHmsEnhet(brukersKommunenummer).enhetNr

            val piloter = mutableListOf<Pilot>()

            if (isDev() || PILOTENHETER_BESTILLE_IKKE_FASTE_LAGERVARER.contains(brukersEnhetnr)) {
                piloter.add(Pilot.BESTILLE_IKKE_FASTE_LAGERVARER)
            }

            piloter
        }
    }.await()
}

