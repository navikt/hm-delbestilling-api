package no.nav.hjelpemidler.delbestilling.infrastructure.oebs

import no.nav.hjelpemidler.delbestilling.common.Lager
import no.nav.hjelpemidler.delbestilling.fakes.NorgResponse
import no.nav.hjelpemidler.delbestilling.runWithTestContext
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FinnLagerenhetTest {

    @Test
    fun `enhet 4702 Akershus skal mappes til 4703 Oslo`() = runWithTestContext {
        norgClient.response = NorgResponse.enhet(enhetNr = "4702")
        val lager = oebs.finnLagerenhet("1234")
        assertEquals(Lager.OSLO, lager)
    }

    @Test
    fun `kommuner i Troms skal mappes til 4719 Troms`() = runWithTestContext {
        norgClient.response = NorgResponse.enhet(enhetNr = "4719")
        val lager = oebs.finnLagerenhet("5512")
        assertEquals(Lager.TROMS, lager)
    }

    @Test
    fun `kommuner i Finnmark skal mappes til 4720 Finnmark`() = runWithTestContext {
        norgClient.response = NorgResponse.enhet(enhetNr = "4719")
        val lager = oebs.finnLagerenhet("5610")
        assertEquals(Lager.FINNMARK, lager)
    }

    @Test
    fun `kommuner som tidligere tilhørte Sør-Trøndelag skal mappes til 4716 Sør-Trøndelag`() = runWithTestContext {
        norgClient.response = NorgResponse.enhet(enhetNr = "4716") // Nav hjelpemiddelsentral Trøndelag
        val lager = oebs.finnLagerenhet("5001") // Trondheim
        assertEquals(Lager.SØR_TRØNDELAG, lager)
    }

    @Test
    fun `kommuner som tidligere tilhørte Nord-trøndelag skal mappes til 4717 Nord-Trøndelag`() = runWithTestContext {
        norgClient.response = NorgResponse.enhet(enhetNr = "4716") // Nav hjelpemiddelsentral Trøndelag
        val lager = oebs.finnLagerenhet("5060") // Nærøysund
        assertEquals(Lager.NORD_TRØNDELAG, lager)
    }
}