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
        val enhetnrTrøndelag = "4716"
        norgClient.response = NorgResponse.enhet(enhetNr = enhetnrTrøndelag)

        val piloter = piloterService.hentPiloter("1234")

        assertEquals(emptyList(), piloter)
    }
}