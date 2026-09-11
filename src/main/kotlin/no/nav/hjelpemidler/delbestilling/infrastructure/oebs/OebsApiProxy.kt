package no.nav.hjelpemidler.delbestilling.infrastructure.oebs

import no.nav.hjelpemidler.domain.person.Fødselsnummer

interface OebsApiProxy {
    suspend fun hentUtlånPåArtnrOgSerienr(artnr: String, serienr: String): UtlånMedSerienrResponse

    suspend fun hentUtlånPåArtnrOgBrukernr(artnr: String, brukernr: String): UtlånResponse

    suspend fun hentUtlånPåArtnr(artnr: String): List<UtlånMedSerienr>

    suspend fun hentPersoninfo(fnr: String): List<OebsPersoninfo>

    suspend fun hentBrukerpassinfo(fnr: String): Brukerpass

    suspend fun hentLagerstatusForEnhetnr(enhetnr: String, hmsnrs: List<String>): List<LagerstatusResponse>

    suspend fun hentFnr(brukernr: String): Fødselsnummer
}