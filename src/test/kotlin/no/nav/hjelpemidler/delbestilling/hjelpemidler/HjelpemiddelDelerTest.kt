package no.nav.hjelpemidler.delbestilling.hjelpemidler

import no.nav.hjelpemidler.delbestilling.hjelpemidler.HjelpemiddelDeler.hentHjelpemiddelMedDeler
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class HjelpemiddelDelerTest {

    @Test
    fun `skal finne hjelpemiddel med deler`() {
        val hmsnrPanthera = "022492"
        val hjelpemiddel = hentHjelpemiddelMedDeler(hmsnrPanthera)
        assertNotNull(hjelpemiddel)

        val deler = hjelpemiddel!!.deler!!
        assertEquals(13, deler.size)

        val hmsnrSchwalbeDekk = "150817"
        val dekk = deler.find { it.hmsnr == hmsnrSchwalbeDekk }!!
        assertEquals("Dekk Schwalbe Marathon Plus punkteringsbeskyttet 24\"x1", dekk.navn)
        assertEquals("Dekk", dekk.kategori)
    }
}