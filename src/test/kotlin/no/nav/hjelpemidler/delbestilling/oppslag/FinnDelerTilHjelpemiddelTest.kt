package no.nav.hjelpemidler.delbestilling.oppslag

import no.nav.hjelpemidler.delbestilling.common.Kilde
import no.nav.hjelpemidler.delbestilling.fakes.GrunndataTestHmsnr
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.data.hmsnr2Hjm
import no.nav.hjelpemidler.delbestilling.runWithTestContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertNull

class FinnDelerTilHjelpemiddelTest {

    @Test
    fun `skal kaste feil når hjelpemiddel ikke finnes i noen kilde`() = runWithTestContext {
        val hmsnr = "000000"

        val exception = runCatching { finnDelerTilHjelpemiddel(hmsnr) }.exceptionOrNull()

        assertTrue(exception is TilbyrIkkeHjelpemiddelException)
    }

    @Test
    fun `skal returnere deler fra manuell liste når hjelpemiddel ikke finnes i grunndata`() = runWithTestContext {
        val hmsnr = GrunndataTestHmsnr.IKKE_I_GRUNNDATA

        // Forutsetninger
        assertNull(grunndata.hentProdukt(hmsnr))

        val hjelpemiddel = finnDelerTilHjelpemiddel(hmsnr)

        // Valider
        assertTrue(hjelpemiddel.deler.all { it.kilde == Kilde.MANUELL_LISTE })
        assertHmsnrEquals(hmsnr2Hjm[hmsnr]!!.deler, hjelpemiddel.deler)
    }

    @Test
    fun `skal returnere deler fra manuell liste når hjelpemiddel finnes uten deler i grunndata`() = runWithTestContext {
        val hmsnr = GrunndataTestHmsnr.UTEN_DELER_I_GRUNNDATA

        // Forutsetninger
        val produkt = grunndata.hentProdukt(hmsnr)!!
        assertTrue(grunndata.hentDeler(seriesId = produkt.seriesId, produktId = produkt.id).isEmpty())
        assertTrue(hmsnr2Hjm[hmsnr]!!.deler.isNotEmpty())

        val hjelpemiddel = finnDelerTilHjelpemiddel(hmsnr)

        // Valider
        assertTrue(hjelpemiddel.deler.all { it.kilde == Kilde.MANUELL_LISTE })
        assertHmsnrEquals(hmsnr2Hjm[hmsnr]!!.deler, hjelpemiddel.deler)
    }

    @Test
    fun `skal returnere deler fra grunndata når hjelpemiddel ikke finnes i manuell liste`() = runWithTestContext {
        val hmsnr = GrunndataTestHmsnr.KUN_GRUNNDATA_DELER

        // Forutsetninger
        assertNull(hmsnr2Hjm[hmsnr])

        val hjelpemiddel = finnDelerTilHjelpemiddel(hmsnr)

        // Valider
        assertTrue(hjelpemiddel.deler.isNotEmpty())
        assertTrue(hjelpemiddel.deler.all { it.kilde == Kilde.GRUNNDATA })
    }

    @Test
    fun `skal supplere deler fra grunndata med deler fra manuell liste`() = runWithTestContext {
        val hmsnr = GrunndataTestHmsnr.GRUNNDATA_OG_MANUELL

        val hjelpemiddel = finnDelerTilHjelpemiddel(hmsnr)

        // Valider
        assertTrue(hjelpemiddel.deler.filter { it.kilde == Kilde.GRUNNDATA }.size > 20) { "Skal finnes noen deler fra grunndata" }
        assertTrue(hjelpemiddel.deler.any { it.kilde == Kilde.MANUELL_LISTE }) { "Skal finnes del kun fra manuell liste" }
    }

    @Test
    fun `skal bruke info fra grunndata dersom del finnes både i grunndata og i manuell liste`() = runWithTestContext {
        val hmsnr = GrunndataTestHmsnr.GRUNNDATA_OG_MANUELL

        val hjelpemiddel = finnDelerTilHjelpemiddel(hmsnr)

        // Valider
        val manuelleDeler = hmsnr2Hjm[hmsnr]!!.deler.map { it.hmsnr }.toSet()
        assertTrue(hjelpemiddel.deler.any { it.kilde == Kilde.GRUNNDATA && it.hmsnr in manuelleDeler })
    }

    private fun assertHmsnrEquals(expected: List<Del>, actual: List<Del>) {
        assertEquals(expected.map { it.hmsnr }.toSet(), actual.map { it.hmsnr }.toSet())
    }
}