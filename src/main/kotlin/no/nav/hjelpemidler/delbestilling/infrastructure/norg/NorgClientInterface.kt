package no.nav.hjelpemidler.delbestilling.infrastructure.norg

import no.nav.hjelpemidler.hjelpemidlerdigitalSoknadapi.tjenester.norg.ArbeidsfordelingEnhet

interface NorgClientInterface {
    suspend fun hentArbeidsfordelingenheter(kommunenummer: String): List<ArbeidsfordelingEnhet>
}