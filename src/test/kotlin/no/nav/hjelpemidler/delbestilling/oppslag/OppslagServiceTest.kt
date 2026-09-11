package no.nav.hjelpemidler.delbestilling.oppslag

import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.PdlResponseMissingData
import no.nav.hjelpemidler.delbestilling.runWithTestContext
import no.nav.hjelpemidler.delbestilling.testdata.PdlRespons
import no.nav.hjelpemidler.delbestilling.testdata.Testdata
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class OppslagServiceTest {

    @Test
    fun `(happy path) skal slå opp hjelpemiddel og berike med lagerstatus`() = runWithTestContext {
        val result = oppslagService.slåOppHjelpemiddelMedSerienr(Testdata.defaultHjmHmsnr, Testdata.defaultHjmSerienr)
        assertTrue(result is OppslagResult.Suksess)
        assertTrue(result.resultat.hjelpemiddel.deler.isNotEmpty())
        assertTrue(result.resultat.hjelpemiddel.deler.all { it.lagerstatus != null })
    }

    @Test
    fun `skal returnere feil dersom utlånet ikke finnes`() = runWithTestContext {
        oebsApiProxy.utlånMedSerienr = null

        val result = oppslagService.slåOppHjelpemiddelMedSerienr(Testdata.defaultHjmHmsnr, Testdata.defaultHjmSerienr)

        assertTrue(result is OppslagResult.Feil)
        assertEquals(OppslagFeil.INGET_UTLÅN, result.feil)
    }

    @Test
    fun `skal kaste feil dersom personen, som utlånet er registrert på, mangler kommunenummer i PDL`() = runWithTestContext {
        pdlClient.response = PdlRespons.person(kommunenummer = null)
        assertFailsWith<PdlResponseMissingData> {
            oppslagService.slåOppHjelpemiddelMedSerienr(Testdata.defaultHjmHmsnr, Testdata.defaultHjmSerienr)
        }
    }

}