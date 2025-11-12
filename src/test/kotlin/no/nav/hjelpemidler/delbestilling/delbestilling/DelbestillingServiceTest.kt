package no.nav.hjelpemidler.delbestilling.delbestilling

import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.OebsPersoninfo
import no.nav.hjelpemidler.delbestilling.testdata.PdlRespons
import no.nav.hjelpemidler.delbestilling.testdata.Testdata
import no.nav.hjelpemidler.delbestilling.testdata.delLinje
import no.nav.hjelpemidler.delbestilling.testdata.delbestillingRequest
import no.nav.hjelpemidler.delbestilling.testdata.fixtures.hentDelUtenDekning
import no.nav.hjelpemidler.delbestilling.testdata.fixtures.hentDelbestillinger
import no.nav.hjelpemidler.delbestilling.testdata.fixtures.hentDelerUtenDekning
import no.nav.hjelpemidler.delbestilling.testdata.fixtures.opprettDelbestilling
import no.nav.hjelpemidler.delbestilling.testdata.fixtures.opprettDelbestillingMedDel
import no.nav.hjelpemidler.delbestilling.runWithTestContext
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class DelbestillingServiceTest {

    @Test
    fun `opprettDelbestilling happy path`() = runWithTestContext {
        assertEquals(0, hentDelbestillinger().size)

        opprettDelbestilling()
        with(hentDelbestillinger()) {
            assertEquals(1, size)
            assertEquals(1, first().saksnummer)
        }

        opprettDelbestilling()
        with(hentDelbestillinger()) {
            assertEquals(2, size)
            assertEquals(2, last().saksnummer)
        }
    }

    @Test
    fun `tekniker kan max sende inn 5 delbestillinger for samme artnr & serienr per døgn`() = runWithTestContext {
        repeat(5) { opprettDelbestilling() }

        with(opprettDelbestilling()) {
            assertEquals(DelbestillingFeil.FOR_MANGE_BESTILLINGER_SISTE_24_TIMER, feil)
        }

        with(hentDelbestillinger()) {
            assertEquals(5, size)
        }
    }

    @Test
    fun `skal ikke lagre delbestilling dersom sending til OEBS feiler`() = runWithTestContext {
        oebsSink.skalKasteFeil = true

        assertThrows<RuntimeException> { opprettDelbestilling() }
        assertEquals(0, hentDelbestillinger().size)
    }

    @Test
    fun `skal feile dersom PDL og OEBS sine kommunenr er ulike for bruker`() = runWithTestContext {
        oebsApiProxy.personinfo = listOf(OebsPersoninfo(Testdata.kommunenummerBergen))
        pdlClient.response = PdlRespons.person(kommunenummer = Testdata.kommunenummerOslo)

        with(opprettDelbestilling()) {
            assertEquals(DelbestillingFeil.ULIK_ADRESSE_PDL_OEBS, feil)
        }
    }

    @Test
    fun `skal lagre anmodningsbehov ved ny delbestilling`() = runWithTestContext {
        val hmsnr = "111111"
        lager.set(hmsnr, antall = 2, minmax = false)

        opprettDelbestillingMedDel(hmsnr = hmsnr, antall = 3)

        with(hentDelUtenDekning(hmsnr)) {
            assertEquals(1, antall, "Skal eksistere anmodningsbehov for 2-3=1stk.")
        }

        delbestillingService.rapporterDelerTilAnmodning()
        with(hentDelerUtenDekning()) {
            assertEquals(0, size, "Det skal ikke lenger eksistere anmodningsbehov")
        }
    }

    @Test
    fun `skal ikke lagre anmodningsbehov dersom del er på minmax`() = runWithTestContext {
        val hmsnr = "111111"
        lager.set(hmsnr, antall = 2, minmax = true)

        opprettDelbestillingMedDel(hmsnr = hmsnr, antall = 7)

        with(hentDelerUtenDekning()) {
            assertEquals(0, size)
        }
    }

    @Test
    fun `skal ikke lage anmodningsbehov ved når det er dekning på lager`() = runWithTestContext {
        val hmsnr = "111111"
        lager.set(hmsnr, antall = 20, minmax = false)

        opprettDelbestillingMedDel(hmsnr = hmsnr, antall = 3)

        with(hentDelerUtenDekning()) {
            assertEquals(0, size)
        }

        with(delbestillingService.rapporterDelerTilAnmodning()) {
            assertEquals(0, size, "Skal ikke lage anmodninger når alle ordre hadde lagerdekning.")
        }
    }

    @Test
    fun `skal ikke lage anmodningsbehov ved etterfylling`() = runWithTestContext {
        val hmsnr = "111111"
        lager.set(hmsnr, antall = 2, minmax = false)

        opprettDelbestillingMedDel(hmsnr = hmsnr, antall = 3)

        with(hentDelUtenDekning(hmsnr)) {
            assertEquals(1, antall)
        }

        lager.set(hmsnr, antall = 5)

        val rapporter = delbestillingService.rapporterDelerTilAnmodning()
        assertEquals(1, rapporter.size)
        with(rapporter.first()) {
            assertEquals(0, anmodningsbehov.size, "Ingen anmodningsbehov for etterfylte deler.")
            assertEquals(1, delerSomIkkeLengerMåAnmodes.size)
            assertTrue(emailClient.outbox.isEmpty(), "Det skal ikke ha blitt sendt mail")
        }
    }

    @Test
    fun `skal summere anmodningsbehov`() = runWithTestContext {
        val hmsnr1 = "111111"
        val hmsnr2 = "222222"
        lager.set(hmsnr1, antall = 2, minmax = false)
        lager.set(hmsnr2, antall = 2, minmax = false)

        opprettDelbestilling(
            delbestillingRequest(
                deler = listOf(
                    delLinje(hmsnr = hmsnr1, antall = 3),
                    delLinje(hmsnr = hmsnr2, antall = 2)
                )
            )
        )

        opprettDelbestilling(
            delbestillingRequest(
                deler = listOf(
                    delLinje(hmsnr = hmsnr1, antall = 2),
                    delLinje(hmsnr = hmsnr2, antall = 2)
                )
            )
        )

        with(hentDelerUtenDekning()) {
            assertEquals(2, size)
            assertEquals(-(2 - 3 - 2), find { it.hmsnr == hmsnr1 }!!.antall)
            assertEquals(-(2 - 2 - 2), find { it.hmsnr == hmsnr2 }!!.antall)
        }
    }
}
