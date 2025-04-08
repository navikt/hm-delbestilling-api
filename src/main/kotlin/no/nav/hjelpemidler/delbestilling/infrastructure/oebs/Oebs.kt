package no.nav.hjelpemidler.delbestilling.infrastructure.oebs

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.delbestilling.model.Lagerstatus

private val log = KotlinLogging.logger {}

class Oebs(
    private val client: OebsApiProxyClient,
) {
    suspend fun hentFnrLeietaker(artnr: String, serienr: String): String? {
        log.info { "Henter leietaker for utlån: artnr=$artnr, serienr=$serienr" }
        return client.hentUtlånPåArtnrOgSerienr(artnr, serienr).utlån?.fnr
    }

    suspend fun hentPersoninfo(fnr: String): List<OebsPersoninfo> {
        log.info { "Henter personinfo" }
        return client.hentPersoninfo(fnr)
    }

    suspend fun harBrukerpass(fnr: String): Boolean {
        log.info { "Sjekker om innlogget bruker har brukerpass" }
        return client.hentBrukerpassinfo(fnr).brukerpass
    }

    suspend fun hentFnrSomHarUtlånPåArtnr(artnr: String): List<Utlån> {
        log.info { "Henter utlån for $artnr" }
        return client.hentFnrSomHarUtlånPåArtnr(artnr)
    }

    suspend fun hentLagerstatusForKommunenummer(kommunenummer: String, hmsnrs: List<String>): List<Lagerstatus> {
        log.info { "Henter lagerstatus for kommunenummer $kommunenummer for hmsnrs $hmsnrs" }
        val response = client.hentLagerstatusForKommunenummer(kommunenummer, hmsnrs)
        return response.map { it.tilLagerstatus() }
    }

    suspend fun hentLagerstatusForEnhetnr(enhetnr: String, hmsnrs: List<String>): List<Lagerstatus> {
        log.info { "Henter lagerstatus for enhetnr $enhetnr for hmsnrs $hmsnrs" }
        val response = client.hentLagerstatusForEnhetnr(enhetnr, hmsnrs)
        return response.map { it.tilLagerstatus() }
    }
}
