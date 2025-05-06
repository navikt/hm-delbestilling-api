package no.nav.hjelpemidler.delbestilling.oppslag.legacy

import no.nav.hjelpemidler.delbestilling.oppslag.legacy.data.hmsnr2Hjm
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.data.hmsnrTilDel
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.data.validerData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

internal class ManuellListeTest {

    @Test
    fun `valider data`() {
        assertDoesNotThrow { validerData() }
    }

    @Test
    fun `skal finne hjelpemiddel med deler for Panthera`() {
        val hmsnrPanthera = "022492"
        val hjelpemiddel = hmsnr2Hjm[hmsnrPanthera]
        assertNotNull(hjelpemiddel)

        val deler = hjelpemiddel!!.deler
        assertTrue(10 < deler.size)

        val hmsnrSchwalbeDekk = "150817"
        val dekk = deler.find { it.hmsnr == hmsnrSchwalbeDekk }!!
        assertEquals("Dekk Schwalbe Marathon Plus punkteringsbeskyttet 24\"x1", dekk.navn)
        assertEquals("Dekk", dekk.kategori)
    }

    @Test
    fun `skal finne hjelpemiddel med deler for Minicrosser M1`() {
        val hmsnrnrMinicrosserM1 = "159629"
        val hjelpemiddel = hmsnr2Hjm[hmsnrnrMinicrosserM1]
        assertNotNull(hjelpemiddel)

        val deler = hjelpemiddel!!.deler
        assertEquals(3, deler.size)

        assertEquals("200842", deler[0].hmsnr)
        assertEquals("Hjul 13x5.00-6 foran/bak", deler[0].navn)
        assertEquals("Hjul", deler[0].kategori)

        assertEquals("328154", deler[1].hmsnr)
        assertEquals("Batteri 85 Ah", deler[1].navn)
        assertEquals("Batteri", deler[1].kategori)
    }

    @Test
    fun `skal finne hjelpemiddel med deler for X850S`() {
        val hmsnrnrX850S = "308941"
        val hjelpemiddel = hmsnr2Hjm[hmsnrnrX850S]
        assertNotNull(hjelpemiddel)

        val deler = hjelpemiddel!!.deler
        assertEquals(3, deler.size)

        assertEquals("309144", deler[0].hmsnr)
        assertEquals("Hjul foran", deler[0].navn)
        assertEquals("Hjul", deler[0].kategori)
    }

    @Test
    fun `skal ikke eksistere deler med defaultAntall større enn maksAntall`() {
        hmsnrTilDel.values.forEach {
            with(it) {
                assertTrue(
                    defaultAntall <= maksAntall,
                    "$hmsnr har defaultAntall $defaultAntall som er større enn maksAntall $maksAntall"
                )
            }
        }
    }

    @Test
    fun `skal ha riktigDefaultAntall på batteri og batteripakke`() {
        val hmsnrnrX850 = "145668"
        val X850 = hmsnr2Hjm[hmsnrnrX850]!!
        val batteriX850 = X850.deler.find { it.kategori == "Batteri" }!!
        assertEquals(2, batteriX850.defaultAntall)

        val hmsnrnrMolift = "161570"
        val molift = hmsnr2Hjm[hmsnrnrMolift]!!
        val batteriMolift = molift.deler.find { it.kategori == "Batteripakke" }!!
        assertEquals(1, batteriMolift.defaultAntall)
    }
}
