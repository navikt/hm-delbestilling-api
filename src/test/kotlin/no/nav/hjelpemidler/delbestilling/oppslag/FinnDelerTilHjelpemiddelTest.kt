package no.nav.hjelpemidler.delbestilling.oppslag

import no.nav.hjelpemidler.delbestilling.common.Kilde
import no.nav.hjelpemidler.delbestilling.fakes.GrunndataTestHmsnr
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.data.hmsnr2Hjm
import no.nav.hjelpemidler.delbestilling.runWithTestContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.Ignore
import kotlin.test.assertNull

class FinnDelerTilHjelpemiddelTest {

    @Test
    fun `skal returnere IkkeFunnet når hjelpemiddel ikke finnes i noen kilde`() = runWithTestContext {
        val hmsnr = "000000"

        val result = finnDelerTilHjelpemiddel(hmsnr)

        assertTrue(result is FinnDelerResultat.IkkeFunnet)
        assertEquals(OppslagFeil.TILBYR_IKKE_HJELPEMIDDEL, (result as FinnDelerResultat.IkkeFunnet).feil)
    }

    @Test
    fun `skal returnere deler fra manuell liste når hjelpemiddel ikke finnes i grunndata`() = runWithTestContext {
        val hmsnr = GrunndataTestHmsnr.IKKE_I_GRUNNDATA

        // Forutsetninger
        assertNull(grunndata.hentProdukt(hmsnr))

        val hjelpemiddel = (finnDelerTilHjelpemiddel(hmsnr) as FinnDelerResultat.Funnet).hjelpemiddel

        // Valider
        assertTrue(hjelpemiddel.deler.all { it.kilde == Kilde.MANUELL_LISTE })
        assertHmsnrEquals(hmsnr2Hjm[hmsnr]!!.deler, hjelpemiddel.deler)
    }

    @Test
    fun `skal returnere deler fra grunndata når hjelpemiddel ikke finnes i manuell liste`() = runWithTestContext {
        val hmsnr = GrunndataTestHmsnr.KUN_GRUNNDATA_DELER

        // Forutsetninger
        assertNull(hmsnr2Hjm[hmsnr])

        val hjelpemiddel = (finnDelerTilHjelpemiddel(hmsnr) as FinnDelerResultat.Funnet).hjelpemiddel

        // Valider
        assertTrue(hjelpemiddel.deler.isNotEmpty())
        assertTrue(hjelpemiddel.deler.all { it.kilde == Kilde.GRUNNDATA })
    }

    private fun assertHmsnrEquals(expected: List<Del>, actual: List<Del>) {
        assertEquals(expected.map { it.hmsnr }.toSet(), actual.map { it.hmsnr }.toSet())
    }
}