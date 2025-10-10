package no.nav.hjelpemidler.delbestilling.fakes

import no.nav.hjelpemidler.delbestilling.common.Lager
import no.nav.hjelpemidler.delbestilling.infrastructure.norg.NorgClientInterface
import no.nav.hjelpemidler.hjelpemidlerdigitalSoknadapi.tjenester.norg.ArbeidsfordelingEnhet


class NorgClientFake() : NorgClientInterface {

    var response: ArbeidsfordelingEnhet = NorgResponse.enhet()

    override suspend fun hentArbeidsfordelingenheter(kommunenummer: String): List<ArbeidsfordelingEnhet> {
        return listOf(response)
    }
}

object NorgResponse {
    fun enhet(
        navn: String = "Nav hjelpemiddelsentral Oslo",
        enhetNr: String = Lager.OSLO.nummer,
        type: String = "HMS"
    ) = ArbeidsfordelingEnhet(navn = navn, enhetNr = enhetNr, type = type)
}