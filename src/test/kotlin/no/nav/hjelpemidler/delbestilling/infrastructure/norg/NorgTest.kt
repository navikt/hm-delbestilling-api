package no.nav.hjelpemidler.delbestilling.infrastructure.norg

import no.nav.hjelpemidler.delbestilling.common.Enhet
import no.nav.hjelpemidler.delbestilling.fakes.NorgResponse
import no.nav.hjelpemidler.delbestilling.testdata.runWithTestContext
import no.nav.hjelpemidler.hjelpemidlerdigitalSoknadapi.tjenester.norg.ArbeidsfordelingEnhet
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class NorgTest {

    @Test
    fun `enhet 4702 Akershus skal mappes til 4703 Oslo`() = runWithTestContext {
        norgClient.response = NorgResponse.enhet(enhetNr = "4702")
        val enhet = norg.hentEnhet("1234")
        assertEquals(Enhet.OSLO, enhet)
    }
}