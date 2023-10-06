package no.nav.hjelpemidler.delbestilling.delbestilling

import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import no.nav.hjelpemidler.delbestilling.MockException
import no.nav.hjelpemidler.delbestilling.TestDatabase
import no.nav.hjelpemidler.delbestilling.delbestillerRolle
import no.nav.hjelpemidler.delbestilling.delbestillingRequest
import no.nav.hjelpemidler.delbestilling.kommune
import no.nav.hjelpemidler.delbestilling.oebs.OebsPersoninfo
import no.nav.hjelpemidler.delbestilling.oebs.OebsService
import no.nav.hjelpemidler.delbestilling.oebs.OpprettBestillingsordreRequest
import no.nav.hjelpemidler.delbestilling.oppslag.OppslagService
import no.nav.hjelpemidler.delbestilling.pdl.PdlService
import no.nav.hjelpemidler.delbestilling.roller.RolleService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class DelbestillingServiceTest {

    val bestillerFnr = "123"
    val bestillerTokenString = "abc"
    val teknikerNavn = "Turid Tekniker"
    val brukersKommunenr = "1234"
    val oebsOrdrenummer = "8725414"

    private var ds = TestDatabase.testDataSource
    private val delbestillingRepository = DelbestillingRepository(ds)
    private val pdlService = mockk<PdlService>().apply {
        coEvery { hentKommunenummer(any()) } returns brukersKommunenr
        coEvery { hentPersonNavn(any(), any()) } returns teknikerNavn
    }
    private val oebsService = mockk<OebsService>(relaxed = true).apply {
        coEvery { hentPersoninfo(any()) } returns listOf(OebsPersoninfo(brukersKommunenr))
    }
    private val rolleService = mockk<RolleService>(relaxed = true).apply {
        coEvery { hentDelbestillerRolle(any()) } returns delbestillerRolle()
    }
    private val oppslagService = mockk<OppslagService>(relaxed = true).apply {
        coEvery { hentKommune(any()) } returns kommune()
    }
    private val delbestillingService =
        DelbestillingService(delbestillingRepository, pdlService, oebsService, rolleService, oppslagService, mockk(relaxed = true))

    @BeforeEach
    fun setup() {
        TestDatabase.cleanAndMigrate(ds)
    }

    @Test
    fun `opprettDelbestilling happy path`() = runTest {
        assertEquals(0, delbestillingService.hentDelbestillinger(bestillerFnr).size)

        delbestillingService.opprettDelbestilling(delbestillingRequest(), bestillerFnr, bestillerTokenString)
        var delebestillinger = delbestillingService.hentDelbestillinger(bestillerFnr)
        assertEquals(1, delebestillinger.size)
        assertEquals(1, delebestillinger.first().saksnummer)

        delbestillingService.opprettDelbestilling(delbestillingRequest(), bestillerFnr, bestillerTokenString)
        delebestillinger = delbestillingService.hentDelbestillinger(bestillerFnr)
        assertEquals(2, delebestillinger.size)
        assertEquals(2, delebestillinger.last().saksnummer)
    }

    @Test
    fun `tekniker kan max sende inn 5 delbestillinger for samme artnr & serienr per døgn`() = runTest {
        delbestillingService.opprettDelbestilling(delbestillingRequest(), bestillerFnr, bestillerTokenString)
        delbestillingService.opprettDelbestilling(delbestillingRequest(), bestillerFnr, bestillerTokenString)
        delbestillingService.opprettDelbestilling(delbestillingRequest(), bestillerFnr, bestillerTokenString)
        delbestillingService.opprettDelbestilling(delbestillingRequest(), bestillerFnr, bestillerTokenString)
        delbestillingService.opprettDelbestilling(delbestillingRequest(), bestillerFnr, bestillerTokenString)
        val resultat =
            delbestillingService.opprettDelbestilling(delbestillingRequest(), bestillerFnr, bestillerTokenString)
        assertEquals(DelbestillingFeil.FOR_MANGE_BESTILLINGER_SISTE_24_TIMER, resultat.feil)

        val bestillinger = delbestillingService.hentDelbestillinger(bestillerFnr)
        assertEquals(5, bestillinger.size)
    }

    @Test
    fun `skal ikke lagre delbestilling dersom sending til OEBS feiler`() = runTest {
        coEvery { oebsService.sendDelbestilling(any()) } throws MockException("Kafka er nede")
        assertEquals(0, delbestillingService.hentDelbestillinger(bestillerFnr).size)
        assertThrows<MockException> {
            delbestillingService.opprettDelbestilling(delbestillingRequest(), bestillerFnr, bestillerTokenString)
        }
        assertEquals(0, delbestillingService.hentDelbestillinger(bestillerFnr).size)

        coEvery { oebsService.sendDelbestilling(any()) } just runs
        delbestillingService.opprettDelbestilling(delbestillingRequest(), bestillerFnr, bestillerTokenString)
        assertEquals(1, delbestillingService.hentDelbestillinger(bestillerFnr).size)
    }

    @Test
    fun `skal sende med riktig info til 5-17 skjema`() = runTest {
        val slot = slot<OpprettBestillingsordreRequest>()
        coEvery { oebsService.sendDelbestilling(capture(slot)) } returns Unit
        delbestillingService.opprettDelbestilling(delbestillingRequest(), bestillerFnr, bestillerTokenString)
        assertEquals("XK-Lager Tekniker: Turid Tekniker", slot.captured.forsendelsesinfo)
    }

    @Test
    fun `skal feile dersom PDL og OEBS sine kommunenr er ulike for bruker`() = runTest {
        coEvery { oebsService.hentPersoninfo(any()) } returns listOf(OebsPersoninfo("0000"))
        val resultat = delbestillingService
            .opprettDelbestilling(delbestillingRequest(), bestillerFnr, bestillerTokenString)
        assertEquals(DelbestillingFeil.ULIK_ADRESSE_PDL_OEBS, resultat.feil)
    }

    @Test
    fun `skal ikke kunne oppdatere delbestilling til en tidligere status`() = runTest {
        coEvery { oebsService.sendDelbestilling(any()) } just runs
        delbestillingService.opprettDelbestilling(delbestillingRequest(), bestillerFnr, bestillerTokenString)
        val delbestilling = delbestillingService.hentDelbestillinger(bestillerFnr).first()
        delbestillingService.oppdaterStatus(delbestilling.saksnummer, Status.KLARGJORT, oebsOrdrenummer)

        // Denne skal ikke ha noen effekt
        delbestillingService.oppdaterStatus(delbestilling.saksnummer, Status.REGISTRERT, oebsOrdrenummer)

        assertEquals(Status.KLARGJORT, delbestillingService.hentDelbestillinger(bestillerFnr).first().status)
    }

    @Test
    fun `skal oppdatere delbestilling status`() = runTest {
        coEvery { oebsService.sendDelbestilling(any()) } just runs
        delbestillingService.opprettDelbestilling(delbestillingRequest(), bestillerFnr, bestillerTokenString)
        val delbestilling = delbestillingService.hentDelbestillinger(bestillerFnr).first()
        assertEquals(Status.INNSENDT, delbestilling.status)
        assertEquals(null, delbestilling.oebsOrdrenummer)

        delbestillingService.oppdaterStatus(delbestilling.saksnummer, Status.KLARGJORT, oebsOrdrenummer)
        val oppdatertDelbestilling = delbestillingService.hentDelbestillinger(bestillerFnr).first()
        assertEquals(Status.KLARGJORT, oppdatertDelbestilling.status)
        assertEquals(oebsOrdrenummer, oppdatertDelbestilling.oebsOrdrenummer)
    }

    @Test
    fun `statusoppdatering skal feile når det er mismatch i oebsOrdrenummer`() = runTest {
        coEvery { oebsService.sendDelbestilling(any()) } just runs
        delbestillingService.opprettDelbestilling(delbestillingRequest(), bestillerFnr, bestillerTokenString)
        val delbestilling = delbestillingService.hentDelbestillinger(bestillerFnr).first()
        delbestillingService.oppdaterStatus(delbestilling.saksnummer, Status.REGISTRERT, oebsOrdrenummer)
        assertThrows<IllegalStateException> {
            delbestillingService.oppdaterStatus(delbestilling.saksnummer, Status.REGISTRERT, "123")
        }
    }

    @Test
    fun `skal oppdatere status på dellinjer`() = runTest {
        coEvery { oebsService.sendDelbestilling(any()) } just runs
        delbestillingService.opprettDelbestilling(delbestillingRequest(), bestillerFnr, bestillerTokenString)
        var delbestilling = delbestillingService.hentDelbestillinger(bestillerFnr).first()
        delbestillingService.oppdaterStatus(delbestilling.saksnummer, Status.KLARGJORT, oebsOrdrenummer)
        val datoOppdatert = LocalDate.of(2023, 9, 29)

        // Skipningsbekreft første del
        delbestillingService.oppdaterDellinjeStatus(
            oebsOrdrenummer,
            DellinjeStatus.SKIPNINGSBEKREFTET,
            delbestilling.delbestilling.deler[0].del.hmsnr,
            datoOppdatert
        )
        delbestilling = delbestillingService.hentDelbestillinger(bestillerFnr).first()
        assertEquals(Status.DELVIS_SKIPNINGSBEKREFTET, delbestilling.status)
        assertEquals(DellinjeStatus.SKIPNINGSBEKREFTET, delbestilling.delbestilling.deler[0].status)
        assertEquals(datoOppdatert, delbestilling.delbestilling.deler[0].datoSkipningsbekreftet)
        assertEquals(LocalDate.of(2023, 10, 2), delbestilling.delbestilling.deler[0].forventetLeveringsdato)

        assertEquals(null, delbestilling.delbestilling.deler[1].status)
        assertEquals(null, delbestilling.delbestilling.deler[1].datoSkipningsbekreftet)
        assertEquals(null, delbestilling.delbestilling.deler[1].forventetLeveringsdato)

        // Skipningsbekreft andre del
        delbestillingService.oppdaterDellinjeStatus(
            oebsOrdrenummer,
            DellinjeStatus.SKIPNINGSBEKREFTET,
            delbestilling.delbestilling.deler[1].del.hmsnr,
            datoOppdatert
        )
        delbestilling = delbestillingService.hentDelbestillinger(bestillerFnr).first()
        assertEquals(Status.SKIPNINGSBEKREFTET, delbestilling.status)
        assertEquals(DellinjeStatus.SKIPNINGSBEKREFTET, delbestilling.delbestilling.deler[0].status)
        assertEquals(DellinjeStatus.SKIPNINGSBEKREFTET, delbestilling.delbestilling.deler[1].status)
    }

    @Test
    fun `skal ignorere skipningsbekreftelse for ukjent ordrenr`() = runTest {
        var delbestilling = delbestillingRepository.withTransaction { tx ->
            delbestillingRepository.hentDelbestilling(tx, oebsOrdrenummer)
        }
        assertNull(delbestilling)

        // Skal bare ignorere ukjent ordrenummer
        assertDoesNotThrow { delbestillingService.oppdaterDellinjeStatus(
            oebsOrdrenummer,
            DellinjeStatus.SKIPNINGSBEKREFTET,
            "123456",
            LocalDate.now(),
        ) }
        
        delbestilling = delbestillingRepository.withTransaction { tx ->
            delbestillingRepository.hentDelbestilling(tx, oebsOrdrenummer)
        }
        assertNull(delbestilling)
    }
}
