package no.nav.hjelpemidler.delbestilling.infrastructure.oebs

interface OebsApiProxy {
    suspend fun hentUtlånPåArtnrOgSerienr(artnr: String, serienr: String): UtlånPåArtnrOgSerienrResponse

    suspend fun hentUtlånPåArtnr(artnr: String): List<Utlån>

    suspend fun hentPersoninfo(fnr: String): List<OebsPersoninfo>

    suspend fun hentBrukerpassinfo(fnr: String): Brukerpass

    suspend fun hentLagerstatusForKommunenummer(kommunenummer: String, hmsnrs: List<String>): List<LagerstatusResponse>

    suspend fun hentLagerstatusForEnhetnr(enhetnr: String, hmsnrs: List<String>): List<LagerstatusResponse>
}