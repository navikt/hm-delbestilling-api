package no.nav.hjelpemidler.delbestilling.oppslag

import no.nav.hjelpemidler.delbestilling.common.Enhet
import no.nav.hjelpemidler.delbestilling.fakes.NorgResponse
import no.nav.hjelpemidler.delbestilling.testdata.Testdata
import no.nav.hjelpemidler.delbestilling.testdata.runWithTestContext
import kotlin.test.Test
import kotlin.test.assertEquals


class PiloterServiceTest {

    @Test
    fun `skal returnere pilot hvis bruker er i pilot-enhetsliste`() = runWithTestContext {
        norgClient.response = NorgResponse.enhet(enhetNr = Enhet.OSLO.nummer)

        val piloter = piloterService.hentPiloter(Testdata.kommunenummerOslo)

        assertEquals(listOf(Pilot.BESTILLE_IKKE_FASTE_LAGERVARER), piloter)
    }

    @Test
    fun `skal returnere tom liste hvis bruker ikke er i pilot`() = runWithTestContext {
        norgClient.response = NorgResponse.enhet(enhetNr = Enhet.TROMS_OG_FINNMARK.nummer)

        val piloter = piloterService.hentPiloter("1234")

        assertEquals(emptyList(), piloter)
    }

    @Test
    fun `skal håndtere gammle enhetsnr`() = runWithTestContext {
        norgClient.response = NorgResponse.enhet(enhetNr = "4702")

        val piloter = piloterService.hentPiloter("1234")

        assertEquals(listOf(Pilot.BESTILLE_IKKE_FASTE_LAGERVARER), piloter)
    }
}