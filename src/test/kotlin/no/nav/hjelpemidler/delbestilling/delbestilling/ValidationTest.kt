package no.nav.hjelpemidler.delbestilling.delbestilling

import no.nav.hjelpemidler.delbestilling.testdata.delLinje
import no.nav.hjelpemidler.delbestilling.testdata.delbestillingRequest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class ValidationTest {

    @Test
    fun `skal returnere feilmelding når delbestilling mangler deler`() {
        val feilmeldinger = validateDelbestillingRequest(delbestillingRequest(deler = emptyList()))
        assertEquals("Delbestillingen må inneholde minst én dellinje", feilmeldinger.first())
    }

    @Test
    fun `skal returnere feilmelding når bestiller mangler opplæring på batteri`() {
        val requestMedOpplæring = delbestillingRequest(
            deler = listOf(
                delLinje(antall = 1, kategori = "Batteri")
            ),
            harOpplæringPåBatteri = true
        )
        assertEquals(0, validateDelbestillingRequest(requestMedOpplæring).size)

        val requestUtenOpplæring = delbestillingRequest(
            deler = listOf(
                delLinje(antall = 1, kategori = "Batteri")
            ),
            harOpplæringPåBatteri = false
        )
        assertEquals(
            "Tekniker må bekrefte opplæring i bytting av batteriene",
            validateDelbestillingRequest(requestUtenOpplæring).first()
        )
    }
}