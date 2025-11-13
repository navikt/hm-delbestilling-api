package no.nav.hjelpemidler.delbestilling.fakes

import no.nav.hjelpemidler.delbestilling.common.Lager
import no.nav.hjelpemidler.delbestilling.fakes.HmsEnhet.HMS_AKERSHUS
import no.nav.hjelpemidler.delbestilling.fakes.HmsEnhet.HMS_OSLO
import no.nav.hjelpemidler.delbestilling.fakes.HmsEnhet.HMS_TROMS_OG_FINNMARK
import no.nav.hjelpemidler.delbestilling.fakes.HmsEnhet.HMS_TRØNDELAG
import no.nav.hjelpemidler.delbestilling.infrastructure.norg.NorgClientInterface
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.ENHETSNUMMER_HMS_AKERSHUS
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.ENHETSNUMMER_HMS_TROMS_OG_FINNMARK
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.ENHETSNUMMER_HMS_TRØNDELAG
import no.nav.hjelpemidler.delbestilling.testdata.Kommunenummer.KARASJOK
import no.nav.hjelpemidler.delbestilling.testdata.Kommunenummer.NÆRØYSUND
import no.nav.hjelpemidler.domain.geografi.Kommune.Companion.BÆRUM
import no.nav.hjelpemidler.domain.geografi.Kommune.Companion.OSLO
import no.nav.hjelpemidler.domain.geografi.Kommune.Companion.TROMSØ
import no.nav.hjelpemidler.domain.geografi.Kommune.Companion.TRONDHEIM
import no.nav.hjelpemidler.hjelpemidlerdigitalSoknadapi.tjenester.norg.ArbeidsfordelingEnhet


class NorgClientFake : NorgClientInterface {

    private val enhetForKommunenummer = mutableMapOf(
        BÆRUM.nummer to HMS_AKERSHUS,
        OSLO.nummer to HMS_OSLO,
        TRONDHEIM.nummer to HMS_TRØNDELAG,
        NÆRØYSUND to HMS_TRØNDELAG,
        TROMSØ.nummer to HMS_TROMS_OG_FINNMARK,
        KARASJOK to HMS_TROMS_OG_FINNMARK,
    )

    override suspend fun hentArbeidsfordelingenheter(kommunenummer: String): List<ArbeidsfordelingEnhet> {
        val enhet = requireNotNull(enhetForKommunenummer[kommunenummer]) { "Mangler enhet for $kommunenummer" }
        return listOf(enhet)
    }

}

private fun enhet(navn: String, nummer: String) = ArbeidsfordelingEnhet(navn, nummer, "HMS")

private fun Lager.tilEnhet() = enhet(navn, nummer)

object HmsEnhet {
    val HMS_AKERSHUS = enhet("Akershus", ENHETSNUMMER_HMS_AKERSHUS)
    val HMS_OSLO = Lager.OSLO.tilEnhet()
    val HMS_TRØNDELAG = enhet("Trøndelag", nummer = ENHETSNUMMER_HMS_TRØNDELAG)
    val HMS_TROMS_OG_FINNMARK = enhet("Troms og Finnmark", ENHETSNUMMER_HMS_TROMS_OG_FINNMARK)
}