package no.nav.hjelpemidler.delbestilling.infrastructure.oebs

import no.nav.hjelpemidler.delbestilling.common.Lager
import no.nav.hjelpemidler.delbestilling.fakes.NorgResponse
import no.nav.hjelpemidler.delbestilling.testdata.runWithTestContext
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
}