package no.nav.hjelpemidler.delbestilling.oppslag

import no.nav.hjelpemidler.delbestilling.common.Kilde
import no.nav.hjelpemidler.delbestilling.fakes.GrunndataTestHmsnr
import no.nav.hjelpemidler.delbestilling.runWithTestContext
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

private const val HJELPEMIDDEL = GrunndataTestHmsnr.KUN_GRUNNDATA_DELER

private fun medHmsnr(hmsnr: String) = DelOppslagRequest.MedHmsnr(hmsnr, HJELPEMIDDEL)
private fun medLevArtNr(levArtNr: String, hjelpemiddelHmsnr: String = HJELPEMIDDEL) =
    DelOppslagRequest.MedLevArtNr(levArtNr, hjelpemiddelHmsnr)

class FinnDelTest {

    @Test
    fun `skal finne del i grunndata`() = runWithTestContext {
        val result = finnDel(medHmsnr("150817"))
        assertIs<FinnDelResultat.Funnet>(result)
        assertEquals("Dekk", result.del.kategori)
        assertEquals(Kilde.GRUNNDATA, result.del.kilde)
        assertEquals(4, result.del.maksAntall)
    }

    @Test
    fun `skal gi ErHjelpemiddel for hovedprodukt i grunndata`() = runWithTestContext {
        val result = finnDel(medHmsnr(GrunndataTestHmsnr.KUN_GRUNNDATA_DELER))
        assertIs<FinnDelResultat.ErHjelpemiddel>(result)
        assertEquals("Comet Alpine", result.hjelpemiddelnavn)
    }

    @Test
    fun `skal gi ErHjelpemiddel for hjelpemiddel som kun finnes i manuell liste`() = runWithTestContext {
        val result = finnDel(medHmsnr("159629"))
        assertIs<FinnDelResultat.ErHjelpemiddel>(result)
    }

    @Test
    fun `skal falle tilbake på manuell liste for del som ikke finnes i grunndata`() = runWithTestContext {
        val result = finnDel(medHmsnr("200842"))
        assertIs<FinnDelResultat.Funnet>(result)
        assertEquals("Hjul", result.del.kategori)
    }

    @Test
    fun `skal gi IkkeFunnet for ukjent hmsnr`() = runWithTestContext {
        assertIs<FinnDelResultat.IkkeFunnet>(finnDel(medHmsnr("000000")))
    }

    @Test
    fun `skal finne del med levArtNr hos hjelpemiddelets leverandør`() = runWithTestContext {
        val result = finnDel(medLevArtNr("FLERE-LEVERANDORER"))
        assertIs<FinnDelResultat.Funnet>(result)
        assertEquals("999001", result.del.hmsnr)
    }

    @Test
    fun `skal gi IkkeFunnet når levArtNr kun finnes hos annen leverandør`() = runWithTestContext {
        assertIs<FinnDelResultat.IkkeFunnet>(finnDel(medLevArtNr("1000038")))
    }

    @Test
    fun `skal gi IkkeFunnet for levArtNr når hjelpemiddelet ikke finnes i grunndata`() = runWithTestContext {
        assertIs<FinnDelResultat.IkkeFunnet>(finnDel(medLevArtNr("FLERE-LEVERANDORER", GrunndataTestHmsnr.IKKE_I_GRUNNDATA)))
    }

    @Test
    fun `skal gi IkkeFunnet ved flere treff på levArtNr hos samme leverandør`() = runWithTestContext {
        assertIs<FinnDelResultat.IkkeFunnet>(finnDel(medLevArtNr("DUPLIKAT")))
    }

    @Test
    fun `skal gi IkkeFunnet når levArtNr er et hovedprodukt`() = runWithTestContext {
        assertIs<FinnDelResultat.IkkeFunnet>(finnDel(medLevArtNr("1545993")))
    }

    @Test
    fun `skal gi IkkeFunnet for ukjent levArtNr`() = runWithTestContext {
        assertIs<FinnDelResultat.IkkeFunnet>(finnDel(medLevArtNr("finnes-ikke")))
    }
}
