package no.nav.hjelpemidler.delbestilling.ordrestatus

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import no.nav.hjelpemidler.delbestilling.common.DellinjeStatus
import no.nav.hjelpemidler.delbestilling.common.Status
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingRepository
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingService
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.AnmodningService
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.lagerstatus
import no.nav.hjelpemidler.delbestilling.infrastructure.geografi.Kommuneoppslag
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Oebs
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.OebsPersoninfo
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.Pdl
import no.nav.hjelpemidler.delbestilling.infrastructure.slack.Slack
import no.nav.hjelpemidler.delbestilling.testdata.TestDatabase
import no.nav.hjelpemidler.delbestilling.testdata.delbestillerRolle
import no.nav.hjelpemidler.delbestilling.testdata.delbestillingRequest
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import java.time.LocalDate


// TODO rydd opp i testene her. Bruk fakes og opprett egne IT for repository.
class DelbestillingStatusServiceTest {

    val brukersFnr = "26928698180"
    val bestillerFnr = "13820599335"
    val teknikerNavn = "Turid Tekniker"
    val brukersKommunenr = "1234"
    val oebsOrdrenummer = "8725414"

    private var ds = TestDatabase.testDataSource
    private val delbestillingRepository = DelbestillingRepository(ds)
    private val pdl = mockk<Pdl>().apply {
        coEvery { hentKommunenummer(any()) } returns brukersKommunenr
        coEvery { hentFornavn(any()) } returns teknikerNavn
    }
    private val kommuneoppslag = mockk<Kommuneoppslag>(relaxed = true).apply {
        coEvery { kommunenavnOrNull(any()) } returns "Oslo"
    }
    private val lagerstatusMock = listOf(
        lagerstatus(hmsnr = "150817", antall = 10),
        lagerstatus(hmsnr = "278247", antall = 10),
    )
    private val oebs = mockk<Oebs>(relaxed = true).apply {
        coEvery { hentPersoninfo(any()) } returns listOf(OebsPersoninfo(brukersKommunenr))
        coEvery { hentFnrLeietaker(any(), any()) } returns brukersFnr
        coEvery { hentLagerstatusForKommunenummer(any(), any()) } returns lagerstatusMock
    }

    private val slack = mockk<Slack>(relaxed = true)
    private val anmodningService = mockk<AnmodningService>(relaxed = true)
    private val delbestillingService =
        DelbestillingService(
            delbestillingRepository,
            pdl,
            oebs,
            kommuneoppslag,
            mockk(relaxed = true),
            slack,
            anmodningService,
        )

    private val delbestillingStatusService = DelbestillingStatusService(delbestillingRepository, oebs, mockk(relaxed = true))

    @BeforeEach
    fun setup() {
        TestDatabase.cleanAndMigrate(ds)
    }

    @Test
    fun `skal ikke kunne oppdatere delbestilling til en tidligere status`() = runTest {
        delbestillingService.opprettDelbestilling(delbestillingRequest(), bestillerFnr, delbestillerRolle())
        val delbestilling = delbestillingService.hentDelbestillinger(bestillerFnr).first()
        delbestillingStatusService.oppdaterStatus(delbestilling.saksnummer, Status.KLARGJORT, oebsOrdrenummer)

        // Denne skal ikke ha noen effekt
        delbestillingStatusService.oppdaterStatus(delbestilling.saksnummer, Status.REGISTRERT, oebsOrdrenummer)

        kotlin.test.assertEquals(Status.KLARGJORT, delbestillingService.hentDelbestillinger(bestillerFnr).first().status)
    }

    @Test
    fun `skal oppdatere delbestilling status`() = runTest {
        delbestillingService.opprettDelbestilling(delbestillingRequest(), bestillerFnr, delbestillerRolle())
        val delbestilling = delbestillingService.hentDelbestillinger(bestillerFnr).first()
        kotlin.test.assertEquals(Status.INNSENDT, delbestilling.status)
        kotlin.test.assertEquals(null, delbestilling.oebsOrdrenummer)

        delbestillingStatusService.oppdaterStatus(delbestilling.saksnummer, Status.KLARGJORT, oebsOrdrenummer)
        val oppdatertDelbestilling = delbestillingService.hentDelbestillinger(bestillerFnr).first()
        kotlin.test.assertEquals(Status.KLARGJORT, oppdatertDelbestilling.status)
        kotlin.test.assertEquals(oebsOrdrenummer, oppdatertDelbestilling.oebsOrdrenummer)
    }

    @Test
    fun `statusoppdatering skal feile når det er mismatch i oebsOrdrenummer`() = runTest {
        delbestillingService.opprettDelbestilling(delbestillingRequest(), bestillerFnr, delbestillerRolle())
        val delbestilling = delbestillingService.hentDelbestillinger(bestillerFnr).first()
        delbestillingStatusService.oppdaterStatus(delbestilling.saksnummer, Status.REGISTRERT, oebsOrdrenummer)
        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            delbestillingStatusService.oppdaterStatus(delbestilling.saksnummer, Status.REGISTRERT, "123")
        }
    }

    @Test
    fun `skal oppdatere status på dellinjer`() = runTest {
        delbestillingService.opprettDelbestilling(delbestillingRequest(), bestillerFnr, delbestillerRolle())
        var delbestilling = delbestillingService.hentDelbestillinger(bestillerFnr).first()
        delbestillingStatusService.oppdaterStatus(delbestilling.saksnummer, Status.KLARGJORT, oebsOrdrenummer)
        val datoOppdatert = LocalDate.of(2023, 9, 29)

        // Skipningsbekreft første del
        delbestillingStatusService.oppdaterDellinjeStatus(
            oebsOrdrenummer,
            DellinjeStatus.SKIPNINGSBEKREFTET,
            delbestilling.delbestilling.deler[0].del.hmsnr,
            datoOppdatert
        )
        delbestilling = delbestillingService.hentDelbestillinger(bestillerFnr).first()
        kotlin.test.assertEquals(Status.DELVIS_SKIPNINGSBEKREFTET, delbestilling.status)
        kotlin.test.assertEquals(DellinjeStatus.SKIPNINGSBEKREFTET, delbestilling.delbestilling.deler[0].status)
        kotlin.test.assertEquals(datoOppdatert, delbestilling.delbestilling.deler[0].datoSkipningsbekreftet)
        kotlin.test.assertEquals(LocalDate.of(2023, 10, 2), delbestilling.delbestilling.deler[0].forventetLeveringsdato)

        kotlin.test.assertEquals(null, delbestilling.delbestilling.deler[1].status)
        kotlin.test.assertEquals(null, delbestilling.delbestilling.deler[1].datoSkipningsbekreftet)
        kotlin.test.assertEquals(null, delbestilling.delbestilling.deler[1].forventetLeveringsdato)

        // Skipningsbekreft andre del
        delbestillingStatusService.oppdaterDellinjeStatus(
            oebsOrdrenummer,
            DellinjeStatus.SKIPNINGSBEKREFTET,
            delbestilling.delbestilling.deler[1].del.hmsnr,
            datoOppdatert
        )
        delbestilling = delbestillingService.hentDelbestillinger(bestillerFnr).first()
        kotlin.test.assertEquals(Status.SKIPNINGSBEKREFTET, delbestilling.status)
        kotlin.test.assertEquals(DellinjeStatus.SKIPNINGSBEKREFTET, delbestilling.delbestilling.deler[0].status)
        kotlin.test.assertEquals(DellinjeStatus.SKIPNINGSBEKREFTET, delbestilling.delbestilling.deler[1].status)
    }

    @Test
    fun `skal ignorere skipningsbekreftelse for ukjent ordrenr`() = runTest {
        var delbestilling = delbestillingRepository.withTransaction { tx ->
            delbestillingRepository.hentDelbestilling(tx, oebsOrdrenummer)
        }
        kotlin.test.assertNull(delbestilling)

        // Skal bare ignorere ukjent ordrenummer
        org.junit.jupiter.api.assertDoesNotThrow {
            delbestillingStatusService.oppdaterDellinjeStatus(
                oebsOrdrenummer,
                DellinjeStatus.SKIPNINGSBEKREFTET,
                "123456",
                LocalDate.now(),
            )
        }

        delbestilling = delbestillingRepository.withTransaction { tx ->
            delbestillingRepository.hentDelbestilling(tx, oebsOrdrenummer)
        }
        kotlin.test.assertNull(delbestilling)
    }
}