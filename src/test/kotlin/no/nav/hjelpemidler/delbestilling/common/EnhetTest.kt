package no.nav.hjelpemidler.delbestilling.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class EnhetTest {

    @Test
    fun `skal returnere enhetens epostadresse`() {
        val enhetnrOslo = "4703"
        val enhet = Enhet.fraEnhetsnummer(enhetnrOslo)
        assertEquals(enhetnrOslo, enhet.nummer)
        assertEquals("nav.hot.oslo.teknisk.ordrekontor@nav.no", enhet.epost())
    }

    @Test
    fun `skal kaste feil for ukjent enhetsnummer`() {
        val ukjentNummer = "9999"
        val exception = assertFailsWith<IllegalArgumentException> {
            Enhet.fraEnhetsnummer(ukjentNummer)
        }
        assertTrue(exception.message!!.contains("9999"))
    }
}