package no.nav.hjelpemidler.delbestilling.delbestilling

import no.nav.hjelpemidler.delbestilling.delLinje
import no.nav.hjelpemidler.delbestilling.delbestillingRequest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ValidationTest {

    @Test
    fun `skal returnere feilmelding når delbestilling mangler deler`() {
        val feilmeldinger = validateDelbestillingRequest(delbestillingRequest(deler = emptyList()))
        assertEquals("Delbestillingen må inneholde minst én dellinje", feilmeldinger.first())
    }

    @Test
    fun `skal returnere feilmelding når dellinje har for høyt antall`() {
        val feilmeldinger = validateDelbestillingRequest(
            delbestillingRequest(
                deler = listOf(
                    delLinje(antall = 3)
                )
            )
        )
        assertEquals("3 overskrider maks antall (2) for 150817", feilmeldinger.first())
    }
}