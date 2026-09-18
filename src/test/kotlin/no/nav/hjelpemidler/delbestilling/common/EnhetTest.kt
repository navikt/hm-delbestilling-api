package no.nav.hjelpemidler.delbestilling.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class EnhetTest {

    @Test
    fun `skal finne lager og returnere epostadresse`() {
        val enhetnrOslo = "4703"
        val lager = Lager.fraLagernummer(enhetnrOslo)
        assertEquals(enhetnrOslo, lager.nummer)
        assertEquals("nav.hot.oslo.teknisk.ordrekontor@nav.no", lager.epost())
    }

    @Test
    fun `skal returnere epost til lager for anmodningsrapport for Oslo`() {
        assertEquals("nav.hot.oslo.lager@nav.no", Lager.OSLO.epostForAnmodningsrapport())
        assertEquals("nav.hot.nordland.teknisk@nav.no", Lager.NORDLAND.epostForAnmodningsrapport())
    }

    @Test
    fun `skal kaste feil for ukjent enhetsnummer`() {
        val ukjentNummer = "9999"
        val exception = assertFailsWith<IllegalArgumentException> {
            Lager.fraLagernummer(ukjentNummer)
        }
        assertTrue(exception.message!!.contains("9999"))
    }
}
