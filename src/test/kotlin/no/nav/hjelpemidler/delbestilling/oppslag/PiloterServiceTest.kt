package no.nav.hjelpemidler.delbestilling.oppslag

import no.nav.hjelpemidler.delbestilling.runWithTestContext
import no.nav.hjelpemidler.domain.geografi.Kommune.Companion.OSLO
import kotlin.test.Test
import kotlin.test.assertEquals


class PiloterServiceTest {

    @Test
    fun `skal returnere pilot hvis bruker bor i Oslo`() = runWithTestContext {
        val piloter = piloterService.hentPiloter(OSLO.nummer)

        assertEquals(listOf(Pilot.BESTILLE_IKKE_FASTE_LAGERVARER), piloter)
    }

}