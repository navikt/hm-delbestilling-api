package no.nav.hjelpemidler.delbestilling.rapportering

import no.nav.hjelpemidler.delbestilling.runWithTestContext
import no.nav.hjelpemidler.delbestilling.testdata.fixtures.gittDelbestillingUtenLagerdekning
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class MånedsrapportAnmodningsbehovTest {

    @Test
    fun `skal sende rapport om anmodningsbehov for forrige måned`() = runWithTestContext {
        clock.set(LocalDate.of(2025, 10, 5))
        gittDelbestillingUtenLagerdekning()
        rapportering.rapporterAnmodningsbehov()

        clock.set(LocalDate.of(2025, 11, 5))
        rapportering.rapporterMånedligAnmodningsoppsummering()

        assertEquals(MÅNEDSRAPPORT_ANMODNINGER_SUBJECT, emailClient.outbox.last().subject)
    }

    @Test
    fun `skal ikke sende rapport dersom det ikke ble sendt anmodningsbehov forrige måned`() = runWithTestContext {
        rapportering.rapporterMånedligAnmodningsoppsummering()

        assertTrue(emailClient.outbox.isEmpty())
    }

}