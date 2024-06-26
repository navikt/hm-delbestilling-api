package no.nav.hjelpemidler.delbestilling.oebs

class OebsService(
    private val oebsApiProxyClient: OebsApiProxyClient,
    private val oebsSinkClient: OebsSinkClient
) {
    suspend fun hentUtlånPåArtnrOgSerienr(artnr: String, serienr: String): Utlån? {
        return oebsApiProxyClient.hentUtlånPåArtnrOgSerienr(artnr, serienr)
    }

    suspend fun hentPersoninfo(fnr: String): List<OebsPersoninfo> {
        return oebsApiProxyClient.hentPersoninfo(fnr)
    }

    suspend fun sendDelbestilling(opprettBestillingsordreRequest: OpprettBestillingsordreRequest) {
        return oebsSinkClient.sendDelbestilling(opprettBestillingsordreRequest)
    }

    suspend fun harBrukerpass(fnr: String): Boolean {
        return oebsApiProxyClient.hentBrukerpassinfo(fnr).brukerpass
    }

    suspend fun hentFnrSomHarUtlånPåArtnr(artnr: String): List<String> {
        return oebsApiProxyClient.hentFnrSomHarUtlånPåArtnr(artnr)
    }
}
