package no.nav.hjelpemidler.delbestilling.delbestilling

import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import jdk.internal.org.jline.keymap.KeyMap.del
import kotlinx.coroutines.test.runTest
import no.nav.hjelpemidler.delbestilling.MockException
import no.nav.hjelpemidler.delbestilling.TestDatabase
import no.nav.hjelpemidler.delbestilling.delLinje
import no.nav.hjelpemidler.delbestilling.delbestillerRolle
import no.nav.hjelpemidler.delbestilling.delbestilling
import no.nav.hjelpemidler.delbestilling.delbestillingRequest
import no.nav.hjelpemidler.delbestilling.delbestillingSak
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.Grunndata
import no.nav.hjelpemidler.delbestilling.kommune
import no.nav.hjelpemidler.delbestilling.oebs.OebsPersoninfo
import no.nav.hjelpemidler.delbestilling.oebs.OebsService
import no.nav.hjelpemidler.delbestilling.oebs.OpprettBestillingsordreRequest
import no.nav.hjelpemidler.delbestilling.oebs.Utlån
import no.nav.hjelpemidler.delbestilling.oppslag.OppslagService
import no.nav.hjelpemidler.delbestilling.organisasjon
import no.nav.hjelpemidler.delbestilling.pdl.PdlService
import no.nav.hjelpemidler.delbestilling.slack.SlackClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class DelbestillingServiceTest {

    val bestillerFnr = "123"
    val teknikerNavn = "Turid Tekniker"
    val brukersKommunenr = "1234"
    val oebsOrdrenummer = "8725414"

    private var ds = TestDatabase.testDataSource
    private val delbestillingRepository = DelbestillingRepository(ds)
    private val pdlService = mockk<PdlService>().apply {
        coEvery { hentKommunenummer(any()) } returns brukersKommunenr
        coEvery { hentFornavn(any(), any()) } returns teknikerNavn
    }
    private val oebsService = mockk<OebsService>(relaxed = true).apply {
        coEvery { hentPersoninfo(any()) } returns listOf(OebsPersoninfo(brukersKommunenr))
    }
    private val oppslagService = mockk<OppslagService>(relaxed = true).apply {
        coEvery { hentKommune(any()) } returns kommune()
    }
    private val slackClient = mockk<SlackClient>()
    private val grunndata = mockk<Grunndata>()
    private val delbestillingService =
        DelbestillingService(
            delbestillingRepository,
            pdlService,
            oebsService,
            oppslagService,
            mockk(relaxed = true),
            slackClient,
            grunndata,
        )

    @BeforeEach
    fun setup() {
        TestDatabase.cleanAndMigrate(ds)
    }

    @Test
    fun `opprettDelbestilling happy path`() = runTest {
        assertEquals(0, delbestillingService.hentDelbestillinger(bestillerFnr).size)

        delbestillingService.opprettDelbestilling(delbestillingRequest(), bestillerFnr, delbestillerRolle())
        var delebestillinger = delbestillingService.hentDelbestillinger(bestillerFnr)
        assertEquals(1, delebestillinger.size)
        assertEquals(1, delebestillinger.first().saksnummer)

        delbestillingService.opprettDelbestilling(delbestillingRequest(), bestillerFnr, delbestillerRolle())
        delebestillinger = delbestillingService.hentDelbestillinger(bestillerFnr)
        assertEquals(2, delebestillinger.size)
        assertEquals(2, delebestillinger.last().saksnummer)
    }

    @Test
    fun `tekniker kan max sende inn 5 delbestillinger for samme artnr & serienr per døgn`() = runTest {
        delbestillingService.opprettDelbestilling(delbestillingRequest(), bestillerFnr, delbestillerRolle())
        delbestillingService.opprettDelbestilling(delbestillingRequest(), bestillerFnr, delbestillerRolle())
        delbestillingService.opprettDelbestilling(delbestillingRequest(), bestillerFnr, delbestillerRolle())
        delbestillingService.opprettDelbestilling(delbestillingRequest(), bestillerFnr, delbestillerRolle())
        delbestillingService.opprettDelbestilling(delbestillingRequest(), bestillerFnr, delbestillerRolle())
        val resultat =
            delbestillingService.opprettDelbestilling(delbestillingRequest(), bestillerFnr, delbestillerRolle())
        assertEquals(DelbestillingFeil.FOR_MANGE_BESTILLINGER_SISTE_24_TIMER, resultat.feil)

        val bestillinger = delbestillingService.hentDelbestillinger(bestillerFnr)
        assertEquals(5, bestillinger.size)
    }

    @Test
    fun `skal ikke lagre delbestilling dersom sending til OEBS feiler`() = runTest {
        coEvery { oebsService.sendDelbestilling(any()) } throws MockException("Kafka er nede")
        assertEquals(0, delbestillingService.hentDelbestillinger(bestillerFnr).size)
        assertThrows<MockException> {
            delbestillingService.opprettDelbestilling(delbestillingRequest(), bestillerFnr, delbestillerRolle())
        }
        assertEquals(0, delbestillingService.hentDelbestillinger(bestillerFnr).size)

        coEvery { oebsService.sendDelbestilling(any()) } just runs
        delbestillingService.opprettDelbestilling(delbestillingRequest(), bestillerFnr, delbestillerRolle())
        assertEquals(1, delbestillingService.hentDelbestillinger(bestillerFnr).size)
    }

    @Test
    fun `skal sende med riktig info til 5-17 skjema`() = runTest {
        val slot = slot<OpprettBestillingsordreRequest>()
        coEvery { oebsService.sendDelbestilling(capture(slot)) } returns Unit
        delbestillingService.opprettDelbestilling(delbestillingRequest(), bestillerFnr, delbestillerRolle())
        assertEquals("XK-Lager Del bestilt av: Turid Tekniker", slot.captured.forsendelsesinfo)
    }

    @Test
    fun `skal feile dersom PDL og OEBS sine kommunenr er ulike for bruker`() = runTest {
        coEvery { oebsService.hentPersoninfo(any()) } returns listOf(OebsPersoninfo("0000"))
        val resultat = delbestillingService
            .opprettDelbestilling(delbestillingRequest(), bestillerFnr, delbestillerRolle())
        assertEquals(DelbestillingFeil.ULIK_ADRESSE_PDL_OEBS, resultat.feil)
    }

    @Test
    fun `skal ikke kunne oppdatere delbestilling til en tidligere status`() = runTest {
        coEvery { oebsService.sendDelbestilling(any()) } just runs
        delbestillingService.opprettDelbestilling(delbestillingRequest(), bestillerFnr, delbestillerRolle())
        val delbestilling = delbestillingService.hentDelbestillinger(bestillerFnr).first()
        delbestillingService.oppdaterStatus(delbestilling.saksnummer, Status.KLARGJORT, oebsOrdrenummer)

        // Denne skal ikke ha noen effekt
        delbestillingService.oppdaterStatus(delbestilling.saksnummer, Status.REGISTRERT, oebsOrdrenummer)

        assertEquals(Status.KLARGJORT, delbestillingService.hentDelbestillinger(bestillerFnr).first().status)
    }

    @Test
    fun `skal oppdatere delbestilling status`() = runTest {
        coEvery { oebsService.sendDelbestilling(any()) } just runs
        delbestillingService.opprettDelbestilling(delbestillingRequest(), bestillerFnr, delbestillerRolle())
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
        delbestillingService.opprettDelbestilling(delbestillingRequest(), bestillerFnr, delbestillerRolle())
        val delbestilling = delbestillingService.hentDelbestillinger(bestillerFnr).first()
        delbestillingService.oppdaterStatus(delbestilling.saksnummer, Status.REGISTRERT, oebsOrdrenummer)
        assertThrows<IllegalStateException> {
            delbestillingService.oppdaterStatus(delbestilling.saksnummer, Status.REGISTRERT, "123")
        }
    }

    @Test
    fun `skal oppdatere status på dellinjer`() = runTest {
        coEvery { oebsService.sendDelbestilling(any()) } just runs
        delbestillingService.opprettDelbestilling(delbestillingRequest(), bestillerFnr, delbestillerRolle())
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
        assertDoesNotThrow {
            delbestillingService.oppdaterDellinjeStatus(
                oebsOrdrenummer,
                DellinjeStatus.SKIPNINGSBEKREFTET,
                "123456",
                LocalDate.now(),
            )
        }

        delbestilling = delbestillingRepository.withTransaction { tx ->
            delbestillingRepository.hentDelbestilling(tx, oebsOrdrenummer)
        }
        assertNull(delbestilling)
    }

    @Test
    fun `skal feile dersom oppslag inneholder deler uten lagerstatus`() = runTest {
        val azaleaHmsnr = "097765"
        val azaleaSerienr = "123456"
        coEvery { oebsService.hentUtlånPåArtnrOgSerienr(azaleaHmsnr, azaleaSerienr) } returns Utlån(
            fnr = "1234567890",
            artnr = azaleaHmsnr,
            serienr = azaleaSerienr,
            utlånsDato = "2020-05-01"
        )
        coEvery { oebsService.hentLagerstatus(any(), any()) } returns emptyList()

        assertThrows<IllegalStateException> {
            delbestillingService.slåOppHjelpemiddel(azaleaHmsnr, azaleaSerienr)
        }
    }

    @Test
    fun `skal retunere null dersom det ikke eksisterer en tidligere batteribestilling`() = runTest {
        val hmsnr = "145668"
        val serienr = "123456"

        delbestillingRepository.withTransaction(returnGeneratedKeys = true) { tx ->
            delbestillingRepository.lagreDelbestilling(
                tx,
                bestillerFnr,
                "12345678910",
                brukersKommunenr,
                delbestilling(hmsnr = hmsnr, serienr = serienr),
                "Oslo",
                organisasjon(),
                BestillerType.KOMMUNAL
            )
        }
        assertNull(delbestillingService.antallDagerSidenSisteBatteribestilling(hmsnr, serienr))
    }

    @Test
    fun `skal returnere antall dager siden forrige batteribestilling`() = runTest {
        val hmsnr = "145668"
        val serienr = "123456"

        delbestillingRepository.withTransaction(returnGeneratedKeys = true) { tx ->
            delbestillingRepository.lagreDelbestilling(
                tx,
                bestillerFnr,
                "12345678910",
                brukersKommunenr,
                delbestilling(
                    hmsnr = hmsnr,
                    serienr = serienr,
                    deler = listOf(delLinje(), delLinje(kategori = "Batteri"))
                ),
                "Oslo",
                organisasjon(),
                BestillerType.KOMMUNAL
            )
        }
        assertEquals(0, delbestillingService.antallDagerSidenSisteBatteribestilling(hmsnr, serienr))
    }

    @Test
    fun `skal returnere antall dager siden forrige batteribestilling når det finnes flere tidligere batteribestillinger`() =
        runTest {
            val repository = mockk<DelbestillingRepository>().also {
                val delbestilling = delbestilling(deler = listOf(delLinje(), delLinje(kategori = "Batteri")))
                coEvery {
                    it.hentDelbestillinger(
                        any(),
                        any()
                    )
                } returns listOf(
                    delbestillingSak(delbestilling = delbestilling, opprettet = LocalDateTime.now().minusDays(77)),
                    delbestillingSak(delbestilling = delbestilling, opprettet = LocalDateTime.now().minusDays(14)),
                    delbestillingSak(delbestilling = delbestilling, opprettet = LocalDateTime.now().minusDays(102)),
                )
            }
            val delbestillingService = DelbestillingService(
                repository, pdlService,
                oebsService,
                oppslagService,
                mockk(relaxed = true),
                slackClient,
                grunndata,
            )
            assertEquals(14, delbestillingService.antallDagerSidenSisteBatteribestilling("hmsnr", "serienr"))
        }

}
