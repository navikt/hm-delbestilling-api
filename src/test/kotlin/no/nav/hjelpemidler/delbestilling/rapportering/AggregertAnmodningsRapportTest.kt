package no.nav.hjelpemidler.delbestilling.rapportering

import no.nav.hjelpemidler.delbestilling.common.Lager
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.Anmodningrapport
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.AnmodningsbehovForDel
import no.nav.hjelpemidler.delbestilling.TestContext
import no.nav.hjelpemidler.delbestilling.runWithTestContext
import no.nav.hjelpemidler.delbestilling.testdata.fixtures.gittDelbestillingUtenLagerdekning
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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

    @Test
    fun `skal inkludere anmodningsbehov fra alle seks måneder i vinduet`() = runWithTestContext {
        listOf(
            LocalDate.of(2025, 4, 5) to "200000",
            LocalDate.of(2025, 5, 5) to "200001",
            LocalDate.of(2025, 6, 5) to "200002",
            LocalDate.of(2025, 7, 5) to "200003",
            LocalDate.of(2025, 8, 5) to "200004",
            LocalDate.of(2025, 9, 5) to "200005",
            LocalDate.of(2025, 10, 5) to "200006",
        ).forEach { (dato, hmsnr) ->
            lagreAnmodningsbehov(this, dato, hmsnr)
        }

        clock.set(LocalDate.of(2025, 11, 5))
        rapportering.rapporterSeksmånedersAnmodningsoppsummering()

        val body = emailClient.outbox.last().body
        assertEquals(ANMODNINGSRAPPORT_SUBJECT, emailClient.outbox.last().subject)
        assertTrue(body.contains("Periode: 2025-05 - 2025-10"), body)
        assertTrue(body.contains("200001"))
        assertTrue(body.contains("200002"))
        assertTrue(body.contains("200003"))
        assertTrue(body.contains("200004"))
        assertTrue(body.contains("200005"))
        assertTrue(body.contains("200006"))
        assertFalse(body.contains("200000"))
    }

}

private suspend fun lagreAnmodningsbehov(
    context: TestContext,
    dato: LocalDate,
    hmsnr: String,
) {
    context.clock.set(dato)
    context.transaction {
        anmodningDao.lagreAnmodninger(
            Anmodningrapport(
                lager = Lager.OSLO,
                anmodningsbehov = listOf(
                    AnmodningsbehovForDel(
                        hmsnr = hmsnr,
                        navn = "Del $hmsnr",
                        antallBestilt = 1,
                        erPåMinmax = false,
                        antallPåLager = 0,
                        antallSomMåAnmodes = 1,
                        leverandørnavn = "Leverandør",
                    )
                ),
                delerSomIkkeLengerMåAnmodes = emptyList(),
            )
        )
    }
}
