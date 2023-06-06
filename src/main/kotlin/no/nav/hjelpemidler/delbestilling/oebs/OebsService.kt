package no.nav.hjelpemidler.delbestilling.oebs

class OebsService(
    private val oebsClient: OebsApiProxyClient,
    private val oebsSinkClient: OebsSinkClient
) {
    suspend fun hentUtlånPåArtnrOgSerienr(artnr: String, serienr: String): Utlån? {
        return oebsClient.hentUtlånPåArtnrOgSerienr(artnr, serienr)
    }

    suspend fun sendDelbestilling(opprettBestillingsordreRequest: OpprettBestillingsordreRequest) {
        return oebsSinkClient.sendDelbestilling(opprettBestillingsordreRequest)
    }
}