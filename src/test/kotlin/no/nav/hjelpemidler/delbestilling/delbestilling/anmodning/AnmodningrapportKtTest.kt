package no.nav.hjelpemidler.delbestilling.delbestilling.anmodning

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AnmodningrapportKtTest {

    @Test
    fun `skal ikke være annmodningsbehov for del på minmax`() {
        val anmodningsbehov = beregnAnmodningsbehovForDelVedInnsending(del(antall = 13), lagerstatus(antall = 1, minmax = true))
        assertTrue(anmodningsbehov.erPåMinmax)
        assertEquals(0, anmodningsbehov.antallSomMåAnmodes)
    }

    @Test
    fun `skal ikke være annmodningsbehov for del med full lagerdekning`() {
        val anmodningsbehov = beregnAnmodningsbehovForDelVedInnsending(del(antall = 6), lagerstatus(antall = 10))
        assertFalse(anmodningsbehov.erPåMinmax)
        assertEquals(0, anmodningsbehov.antallSomMåAnmodes)
    }

    @Test
    fun `skal være annmodningsbehov likt antall bestilt dersom det er tomt på lager`() {
        val anmodningsbehov = beregnAnmodningsbehovForDelVedInnsending(del(antall = 7), lagerstatus(antall = 0))
        assertEquals(7, anmodningsbehov.antallSomMåAnmodes)
    }

    @Test
    fun `skal være annmodningsbehov likt differansen mellom antall bestilt og antall på lager ved delvis lagerdekning`() {
        val anmodningsbehov = beregnAnmodningsbehovForDelVedInnsending(del(antall = 12), lagerstatus(antall = 4))
        assertEquals(8, anmodningsbehov.antallSomMåAnmodes)
    }

}
