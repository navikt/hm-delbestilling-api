package no.nav.hjelpemidler.delbestilling.hjelpemidler

import no.nav.hjelpemidler.delbestilling.hjelpemidler.HjelpemiddelDeler.hentAlleHjelpemidlerMedDeler
import no.nav.hjelpemidler.delbestilling.hjelpemidler.HjelpemiddelDeler.hentHjelpemiddelMedDeler
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class HjelpemiddelDelerTest {

    @Test
    fun `skal finne hjelpemiddel med deler for Panthera`() {
        val hmsnrPanthera = "022492"
        val hjelpemiddel = hentHjelpemiddelMedDeler(hmsnrPanthera)
        assertNotNull(hjelpemiddel)

        val deler = hjelpemiddel!!.deler!!
        assertEquals(11, deler.size)

        val hmsnrSchwalbeDekk = "150817"
        val dekk = deler.find { it.hmsnr == hmsnrSchwalbeDekk }!!
        assertEquals("Dekk Schwalbe Marathon Plus punkteringsbeskyttet 24\"x1", dekk.navn)
        assertEquals("Dekk", dekk.kategori)
    }

    @Test
    fun `skal hente ut alle hjelpemidler med deler`() {
        val hjelpemidlerMedDeler = hentAlleHjelpemidlerMedDeler()
        val hjelpemiddel = hjelpemidlerMedDeler.first()
        val deler = hjelpemiddel.deler!!

        assertEquals(11, deler.size)
        assertEquals(526, hjelpemidlerMedDeler.size)
    }

    @Test
    fun `skal finne hjelpemiddel med deler for Minicrosser M1`() {
        val hmsnrnrMinicrosserM1 = "159629"
        val hjelpemiddel = hentHjelpemiddelMedDeler(hmsnrnrMinicrosserM1)
        assertNotNull(hjelpemiddel)

        val deler = hjelpemiddel!!.deler!!
        assertEquals(3, deler.size)

        assertEquals("200842", deler[0].hmsnr)
        assertEquals("Hjul foran/bak", deler[0].navn)
        assertEquals("Hjul", deler[0].kategori)

        assertEquals("263773", deler[1].hmsnr)
        assertEquals("Batteri 85 ah", deler[1].navn)
        assertEquals("Batteri", deler[1].kategori)
    }

    @Test
    fun `skal finne hjelpemiddel med deler for X850S`() {
        val hmsnrnrX850S = "308941"
        val hjelpemiddel = hentHjelpemiddelMedDeler(hmsnrnrX850S)
        assertNotNull(hjelpemiddel)

        val deler = hjelpemiddel!!.deler!!
        assertEquals(3, deler.size)

        assertEquals("309144", deler[0].hmsnr)
        assertEquals("Hjul foran", deler[0].navn)
        assertEquals("Hjul", deler[0].kategori)
    }
}
