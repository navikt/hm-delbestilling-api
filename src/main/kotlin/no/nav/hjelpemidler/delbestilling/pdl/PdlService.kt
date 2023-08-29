package no.nav.hjelpemidler.delbestilling.pdl

import no.nav.hjelpemidler.delbestilling.exceptions.PdlRequestFailedException

class PdlService(private val pdlClient: PdlClient) {

    suspend fun hentKommunenummer(fnr: String): String {
        return pdlClient.hentKommunenummer(fnr)
    }

    suspend fun hentPersonNavn(fnr: String, validerAdressebeskyttelse: Boolean = true): String {
        val pdlResponse = pdlClient.hentPersonNavn(fnr, validerAdressebeskyttelse)
        val navneData = pdlResponse.data?.hentPerson?.navn?.get(0)
            ?: throw PdlRequestFailedException("PDL response mangler data")
        val fornavn = if (navneData.mellomnavn.isNullOrBlank()) {
            navneData.fornavn
        } else {
            "${navneData.fornavn} ${navneData.mellomnavn}"
        }
        return "$fornavn ${navneData.etternavn}"
    }

    suspend fun harGodkjentForeldrerelasjonForBrukerpass(bestillerFnr: String, brukersFnr: String): Boolean {
        val pdlResponse = pdlClient.hentForelderBarnRelasjon(bestillerFnr, true)
        val godkjenteForeldreelasjoner = listOf(
            ForelderBarnRelasjonRolle.FAR,
            ForelderBarnRelasjonRolle.MOR,
            ForelderBarnRelasjonRolle.MEDMOR
        )

        // Brukerpassbruker må ha ha en godkjent relasjon til bruker, f.eks være far til et barn
        val harGodkjentRelasjon =
            pdlResponse.data?.hentPerson?.forelderBarnRelasjon?.any {
                it.relatertPersonsIdent == brukersFnr && godkjenteForeldreelasjoner.contains(
                    it.minRolleForPerson
                )
            } ?: false

        return harGodkjentRelasjon
    }
}
