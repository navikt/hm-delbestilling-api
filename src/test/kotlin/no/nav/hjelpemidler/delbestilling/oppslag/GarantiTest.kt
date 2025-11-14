package no.nav.hjelpemidler.delbestilling.oppslag

import no.nav.hjelpemidler.delbestilling.testdata.Testdata.isoERS
import no.nav.hjelpemidler.delbestilling.testdata.Testdata.isoPersonløfter
import no.nav.hjelpemidler.delbestilling.runWithTestContext
import no.nav.hjelpemidler.delbestilling.testdata.utlån
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import java.time.LocalDate.now
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GarantiTest {

    @Test
    fun `skal IKKE være dekket av garanti når ERS er mer enn 3 år gammel`() = runWithTestContext {
        val hjelpemiddel = hjelpemiddel().berikMedGaranti(
            utlån(
                opprettet = now().minusYears(5),
                isokode = isoERS,
            )
        )

        assertEquals(hjelpemiddel.antallÅrGaranti, 3)
        assertFalse(hjelpemiddel.erInnenforGaranti!!)
    }

    @Test
    fun `skal være dekket av garanti når ERS er mindre enn 3 år gammel`() = runWithTestContext {
        val hjelpemiddel = hjelpemiddel().berikMedGaranti(
            utlån(
                opprettet = now().minusYears(1),
                isokode = isoERS
            )
        )

        assertEquals(hjelpemiddel.antallÅrGaranti, 3)
        assertTrue(hjelpemiddel.erInnenforGaranti!!)
    }

    @Test
    fun `skal ikke berike hjelpemiddel hvis utlån mangler opprettetDato`() = runWithTestContext {
        val hjelpemiddel = hjelpemiddel().berikMedGaranti(
            utlån(
                opprettet = null,
                isokode = isoERS
            )
        )

        assertNull(hjelpemiddel.antallÅrGaranti)
        assertNull(hjelpemiddel.erInnenforGaranti)
    }

    @Test
    fun `skal beregne 2 års garanti for hjelpemiddel som ikke er ERS`() = runWithTestContext {
        val hjelpemiddel = hjelpemiddel().berikMedGaranti(
            utlån(
                opprettet = now().minusYears(1),
                isokode = isoPersonløfter
            )
        )

        assertEquals(hjelpemiddel.antallÅrGaranti, 2)
        assertTrue(hjelpemiddel.erInnenforGaranti!!)
    }
}
