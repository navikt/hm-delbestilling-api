package no.nav.hjelpemidler.delbestilling.oebs

class OebsProxyApiService(
    private val oebsProxyApiClient: OebsApiProxyClient
) {
    suspend fun hentUtlånPåArtnrOgSerienr(artnr: String, serienr: String): Utlån? {
        return oebsProxyApiClient.hentUtlånPåArtnrOgSerienr(artnr, serienr)
    }
}