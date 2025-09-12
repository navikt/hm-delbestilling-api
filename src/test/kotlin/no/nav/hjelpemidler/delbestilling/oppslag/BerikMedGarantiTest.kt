package no.nav.hjelpemidler.delbestilling.oppslag

import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Utlån
import no.nav.hjelpemidler.delbestilling.testdata.Testdata
import no.nav.hjelpemidler.delbestilling.testdata.runWithTestContext
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BerikMedGarantiTest {

    @Test
    fun `skal berike hjelpemiddel med erInnenforGaranti=false hvis hjelpemiddel er utenfor garantitid`() = runWithTestContext {
        val utlån = Utlån(
            fnr = "111111111111",
            artnr = "326539",
            serienr = "000000",
            opprettetDato = "2020-01-01 00:00:00",
            utlånsDato = "2025-01-01",
            isokode = "122203"
        )

        val hjelpemiddel = Hjelpemiddel(
            navn = "Cross 6",
            hmsnr = "326539",
            deler = emptyList(),
        )

        val beriket = berikMedGaranti(hjelpemiddel, utlån, LocalDate.parse("2025-01-01"))

        assertEquals(beriket.antallÅrGaranti, 2)
        assertTrue { beriket.erInnenforGaranti == false }
    }

    @Test
    fun `skal berike hjelpemiddel med erInnenforGaranti=true hvis hjelpemiddel er innenfor garantitid`() = runWithTestContext {
        val utlån = Utlån(
            fnr = "111111111111",
            artnr = "326539",
            serienr = "000000",
            opprettetDato = "2024-01-01 00:00:00",
            utlånsDato = "2025-01-01",
            isokode = "122203"
        )

        val hjelpemiddel = Hjelpemiddel(
            navn = "Cross 6",
            hmsnr = "326539",
            deler = emptyList(),
        )

        val beriket = berikMedGaranti(hjelpemiddel, utlån, LocalDate.parse("2025-01-01"))

        assertEquals(beriket.antallÅrGaranti, 2)
        assertTrue { beriket.erInnenforGaranti == true }
    }

    @Test
    fun `skal berike hjelpemiddel med antallÅrGaranti=3 for ERS`() = runWithTestContext {
        val utlån = Utlån(
            fnr = "111111111111",
            artnr = "238378",
            serienr = "000000",
            opprettetDato = "2021-01-01 00:00:00",
            utlånsDato = "2025-01-01",
            isokode = "122303" // Elektriske rullestoler med manuell styring
        )

        val hjelpemiddel = Hjelpemiddel(
            navn = "Comet Alpine Plus",
            hmsnr = "238378",
            deler = emptyList(),
        )

        val beriket = berikMedGaranti(hjelpemiddel, utlån, LocalDate.parse("2025-01-01"))

        assertEquals(beriket.antallÅrGaranti, 3)
        assertTrue { beriket.erInnenforGaranti == false }
    }

    @Test
    fun `skal ikke berike hjelpemiddel hvis utlån mangler opprettetDato`() = runWithTestContext {
        val utlån = Utlån(
            fnr = "111111111111",
            artnr = "238378",
            serienr = "000000",
            opprettetDato = null,
            utlånsDato = "2025-01-01",
            isokode = "122303" // Elektriske rullestoler med manuell styring
        )

        val hjelpemiddel = Hjelpemiddel(
            navn = "Comet Alpine Plus",
            hmsnr = "238378",
            deler = emptyList(),
        )

        val beriket = berikMedGaranti(hjelpemiddel, utlån, LocalDate.parse("2025-01-01"))

        assertNull(beriket.antallÅrGaranti)
        assertNull(beriket.erInnenforGaranti)
    }
}
