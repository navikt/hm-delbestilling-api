package no.nav.hjelpemidler.delbestilling.oppslag

import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.PdlResponseMissingData
import no.nav.hjelpemidler.delbestilling.runWithTestContext
import no.nav.hjelpemidler.delbestilling.testdata.PdlRespons
import no.nav.hjelpemidler.delbestilling.testdata.Testdata
import no.nav.hjelpemidler.domain.geografi.Kommune.Companion.OSLO
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class OppslagServiceTest {

    @Test
    fun `(happy path) skal slå opp hjelpemiddel og berike med lagerstatus`() = runWithTestContext {
        val oppslag = oppslagService.slåOppHjelpemiddel(Testdata.defaultHjmHmsnr, Testdata.defaultHjmSerienr)
        assertTrue(oppslag.hjelpemiddel.deler.isNotEmpty())
        assertTrue(oppslag.hjelpemiddel.deler.all { it.lagerstatus != null })
    }

    @Test
    fun `skal kaste feil dersom utlånet ikke finnes`() = runWithTestContext {
        oebsApiProxy.utlån = null

        val exception = assertFailsWith<IngenUtlånException> {
            oppslagService.slåOppHjelpemiddel(Testdata.defaultHjmHmsnr, Testdata.defaultHjmSerienr)
        }

        assertTrue(exception.message!!.contains(Testdata.defaultHjmHmsnr))
        assertTrue(exception.message!!.contains(Testdata.defaultHjmSerienr))
    }

    @Test
    fun `skal kaste feil dersom personen, som utlånet er registrert på, mangler kommunenummer i PDL`() = runWithTestContext {
        pdlClient.response = PdlRespons.person(kommunenummer = null)
        assertFailsWith<PdlResponseMissingData> {
            oppslagService.slåOppHjelpemiddel(Testdata.defaultHjmHmsnr, Testdata.defaultHjmSerienr)
        }
    }

    @Test
    fun `skal returnere pilot dersom innbygger sogner til Hms Oslo`() = runWithTestContext {
        pdlClient.response = PdlRespons.person(kommunenummer = OSLO.nummer)
        val oppslag = oppslagService.slåOppHjelpemiddel(Testdata.defaultHjmHmsnr, Testdata.defaultHjmSerienr)
        assertTrue(oppslag.piloter.contains(Pilot.BESTILLE_IKKE_FASTE_LAGERVARER))
    }

}