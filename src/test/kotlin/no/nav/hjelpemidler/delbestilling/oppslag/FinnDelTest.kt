package no.nav.hjelpemidler.delbestilling.oppslag

import no.nav.hjelpemidler.delbestilling.common.Kilde
import no.nav.hjelpemidler.delbestilling.fakes.GrunndataTestHmsnr
import no.nav.hjelpemidler.delbestilling.runWithTestContext
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class FinnDelTest {

    @Test
    fun `skal finne del i grunndata`() = runWithTestContext {
        val result = finnDel("150817")
        assertIs<FinnDelResultat.Funnet>(result)
        assertEquals("Dekk", result.del.kategori)
        assertEquals(Kilde.GRUNNDATA, result.del.kilde)
        assertEquals(4, result.del.maksAntall)
    }

    @Test
    fun `skal gi ErHjelpemiddel for hovedprodukt i grunndata`() = runWithTestContext {
        val result = finnDel(GrunndataTestHmsnr.KUN_GRUNNDATA_DELER)
        assertIs<FinnDelResultat.ErHjelpemiddel>(result)
        assertEquals("Comet Alpine", result.hjelpemiddelnavn)
    }

    @Test
    fun `skal gi ErHjelpemiddel for hjelpemiddel som kun finnes i manuell liste`() = runWithTestContext {
        val result = finnDel("159629")
        assertIs<FinnDelResultat.ErHjelpemiddel>(result)
    }

    @Test
    fun `skal falle tilbake på manuell liste for del som ikke finnes i grunndata`() = runWithTestContext {
        val result = finnDel("200842")
        assertIs<FinnDelResultat.Funnet>(result)
        assertEquals("Hjul", result.del.kategori)
    }

    @Test
    fun `skal gi IkkeFunnet for ukjent hmsnr`() = runWithTestContext {
        assertIs<FinnDelResultat.IkkeFunnet>(finnDel("000000"))
    }
}
