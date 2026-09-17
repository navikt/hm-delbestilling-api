package no.nav.hjelpemidler.delbestilling.rapportering

import no.nav.hjelpemidler.delbestilling.runWithTestContext
import no.nav.hjelpemidler.delbestilling.testdata.fixtures.gittDelbestillingUtenLagerdekning
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class AggregertAnmodningsRapportTest {

    @Test
    fun `skal sende rapport om anmodningsbehov for siste seks måneder`() = runWithTestContext {
        clock.set(LocalDate.of(2025, 10, 5))
        gittDelbestillingUtenLagerdekning()
        rapportering.rapporterAnmodningsbehov()

        clock.set(LocalDate.of(2025, 11, 5))
        rapportering.rapporterSeksmånedersAnmodningsoppsummering()

        assertEquals(ANMODNINGSRAPPORT_SUBJECT, emailClient.outbox.last().subject)
        assertTrue(emailClient.outbox.last().body.contains("Periode: 2025-05 - 2025-10"))
    }

    @Test
    fun `skal ikke sende rapport dersom det ikke finnes anmodninger i perioden`() = runWithTestContext {
        rapportering.rapporterSeksmånedersAnmodningsoppsummering()

        assertTrue(emailClient.outbox.isEmpty())
    }

}
