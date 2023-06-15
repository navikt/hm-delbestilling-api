package no.nav.hjelpemidler.delbestilling.delbestilling

import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import no.nav.hjelpemidler.delbestilling.MockException
import no.nav.hjelpemidler.delbestilling.TestDatabase
import no.nav.hjelpemidler.delbestilling.delbestillerRolle
import no.nav.hjelpemidler.delbestilling.delbestillingRequest
import no.nav.hjelpemidler.delbestilling.oebs.OebsPersoninfo
import no.nav.hjelpemidler.delbestilling.oebs.OebsService
import no.nav.hjelpemidler.delbestilling.oebs.OpprettBestillingsordreRequest
import no.nav.hjelpemidler.delbestilling.pdl.PdlService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class DelbestillingServiceTest {

    val bestillerFnr = "123"
    val teknikerNavn = "Turid Tekniker"
    val brukersKommunenr = "1234"

    private var ds = TestDatabase.testDataSource
    private val delbestillingRepository = DelbestillingRepository(ds)
    private val pdlService = mockk<PdlService>().apply {
        coEvery { hentKommunenummer(any()) } returns brukersKommunenr
        coEvery { hentPersonNavn(any(), any()) } returns teknikerNavn
    }
    private val oebsService = mockk<OebsService>(relaxed = true).apply {
        coEvery { hentPersoninfo(any()) } returns listOf(OebsPersoninfo(brukersKommunenr))
    }
    private val delbestillingService = DelbestillingService(ds, delbestillingRepository, pdlService, oebsService)

    @BeforeEach
    fun setup() {
        TestDatabase.cleanAndMigrate(ds)
    }
/*
    @Test
    fun `opprettDelbestilling happy path`() = runTest {
        assertEquals(0, delbestillingService.hentDelbestillinger(bestillerFnr).size)

        delbestillingService.opprettDelbestilling(delbestillerRolle(), delbestillingRequest(), bestillerFnr)
        var delebestillinger = delbestillingService.hentDelbestillinger(bestillerFnr)
        assertEquals(1, delebestillinger.size)
        assertEquals(1, delebestillinger.first().saksnummer)

        delbestillingService.opprettDelbestilling(delbestillerRolle(), delbestillingRequest(), bestillerFnr)
        delebestillinger = delbestillingService.hentDelbestillinger(bestillerFnr)
        assertEquals(2, delebestillinger.size)
        assertEquals(2, delebestillinger.last().saksnummer)
    }

    @Test
    fun `tekniker kan max sende inn 2 delbestillinger per døgn`() = runTest {
        delbestillingService.opprettDelbestilling(delbestillerRolle(), delbestillingRequest(), bestillerFnr)
        delbestillingService.opprettDelbestilling(delbestillerRolle(), delbestillingRequest(), bestillerFnr)
        val resultat = delbestillingService.opprettDelbestilling(delbestillerRolle(), delbestillingRequest(), bestillerFnr)
        assertEquals(DelbestillingFeil.FOR_MANGE_BESTILLINGER_SISTE_24_TIMER, resultat.feil)

        val bestillinger = delbestillingService.hentDelbestillinger(bestillerFnr)
        assertEquals(2, bestillinger.size)
    }

    @Test
    fun `skal ikke lagre delbestilling dersom sending til OEBS feiler`() = runTest {
        coEvery { oebsService.sendDelbestilling(any()) } throws MockException("Kafka er nede")
        assertEquals(0, delbestillingService.hentDelbestillinger(bestillerFnr).size)
        assertThrows<MockException> {
            delbestillingService.opprettDelbestilling(delbestillerRolle(), delbestillingRequest(), bestillerFnr)
        }
        assertEquals(0, delbestillingService.hentDelbestillinger(bestillerFnr).size)
    }

    @Test
    fun `skal sende med riktig info til 5-17 skjema`() = runTest {
        val slot = slot<OpprettBestillingsordreRequest>()
        coEvery { oebsService.sendDelbestilling(capture(slot)) } returns Unit
        delbestillingService.opprettDelbestilling(delbestillerRolle(), delbestillingRequest(), bestillerFnr)
        assertEquals("Sendes til XK-Lager. Tekniker: Turid Tekniker", slot.captured.forsendelsesinfo)
    }

    @Test
    fun `skal feile dersom PDL og OEBS sine kommunenr er ulike for bruker`() = runTest {
        coEvery { oebsService.hentPersoninfo(any()) } returns listOf(OebsPersoninfo("0000"))
        val resultat = delbestillingService
            .opprettDelbestilling(delbestillerRolle(), delbestillingRequest(), bestillerFnr)
        assertEquals(DelbestillingFeil.ULIK_ADRESSE_PDL_OEBS, resultat.feil)
    }
    
 */
}