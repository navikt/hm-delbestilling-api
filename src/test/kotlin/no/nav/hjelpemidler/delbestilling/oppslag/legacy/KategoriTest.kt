package no.nav.hjelpemidler.delbestilling.oppslag.legacy

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class KategoriTest {

    @Test
    fun `hjul med iso 180991 skal ha maksAntall 5`() {
        assertEquals(5, maksAntall("Hjul", "180991"))
    }

    @Test
    fun `hjul med annen iso skal ha maksAntall 4`() {
        assertEquals(4, maksAntall("Hjul", "122393"))
        assertEquals(4, maksAntall("Hjul", "180915"))
    }

    @Test
    fun `annen kategori med iso 180991 skal ha maksAntall 4`() {
        assertEquals(4, maksAntall("Hjulsett", "180991"))
        assertEquals(4, maksAntall("Svinghjul", "180991"))
        assertEquals(4, maksAntall("Gasspatron", "180991"))
    }
}
