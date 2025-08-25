package no.nav.hjelpemidler.delbestilling.ordrestatus

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import no.nav.hjelpemidler.delbestilling.common.DellinjeStatus
import no.nav.hjelpemidler.delbestilling.common.Status
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingService
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.AnmodningService
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.lagerstatus
import no.nav.hjelpemidler.delbestilling.infrastructure.geografi.Kommuneoppslag
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Oebs
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.OebsPersoninfo
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.Pdl
import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction.Transaction
import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction.TransactionScopeFactory
import no.nav.hjelpemidler.delbestilling.infrastructure.slack.Slack
import no.nav.hjelpemidler.delbestilling.testdata.TestDatabase
import no.nav.hjelpemidler.delbestilling.testdata.delbestillerRolle
import no.nav.hjelpemidler.delbestilling.testdata.delbestillingRequest
import no.nav.hjelpemidler.time.TIME_ZONE_EUROPE_OSLO
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.assertDoesNotThrow
import java.time.LocalDate
import java.util.TimeZone
import kotlin.test.assertEquals
import kotlin.test.assertNull


// TODO rydd opp i testene her. Bruk fakes og opprett egne IT for repository.
class DelbestillingStatusServiceTest {

    val brukersFnr = "26928698180"
    val bestillerFnr = "13820599335"
    val teknikerNavn = "Turid Tekniker"
    val brukersKommunenr = "1234"
    val oebsOrdrenummer = "8725414"

    private var ds = TestDatabase.testDataSource
    private val transaction = Transaction(ds, TransactionScopeFactory())
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
            transaction,
            pdl,
            oebs,
            kommuneoppslag,
            mockk(relaxed = true),
            slack,
            anmodningService,
        )

    private val delbestillingStatusService = DelbestillingStatusService(transaction, oebs, mockk(relaxed = true), slack)

    @BeforeEach
    fun setup() {
        TestDatabase.cleanAndMigratedDataSource(ds)
    }

    @Test
    fun `skal ikke kunne oppdatere delbestilling til en tidligere status`() = runTest {
        delbestillingService.opprettDelbestilling(delbestillingRequest(), bestillerFnr, delbestillerRolle())
        val delbestilling = delbestillingService.hentDelbestillinger(bestillerFnr).first()
        delbestillingStatusService.oppdaterStatus(delbestilling.saksnummer, Status.KLARGJORT, oebsOrdrenummer)

        // Denne skal ikke ha noen effekt
        delbestillingStatusService.oppdaterStatus(delbestilling.saksnummer, Status.REGISTRERT, oebsOrdrenummer)

        assertEquals(Status.KLARGJORT, delbestillingService.hentDelbestillinger(bestillerFnr).first().status)
    }

    @Test
    fun `skal oppdatere delbestilling status`() = runTest {
        delbestillingService.opprettDelbestilling(delbestillingRequest(), bestillerFnr, delbestillerRolle())
        val delbestilling = delbestillingService.hentDelbestillinger(bestillerFnr).first()
        assertEquals(Status.INNSENDT, delbestilling.status)
        assertEquals(null, delbestilling.oebsOrdrenummer)

        delbestillingStatusService.oppdaterStatus(delbestilling.saksnummer, Status.KLARGJORT, oebsOrdrenummer)
        val oppdatertDelbestilling = delbestillingService.hentDelbestillinger(bestillerFnr).first()
        assertEquals(Status.KLARGJORT, oppdatertDelbestilling.status)
        assertEquals(oebsOrdrenummer, oppdatertDelbestilling.oebsOrdrenummer)
    }

    @Test
    fun `statusoppdatering skal feile når det er mismatch i oebsOrdrenummer`() = runTest {
        delbestillingService.opprettDelbestilling(delbestillingRequest(), bestillerFnr, delbestillerRolle())
        val delbestilling = delbestillingService.hentDelbestillinger(bestillerFnr).first()
        delbestillingStatusService.oppdaterStatus(delbestilling.saksnummer, Status.REGISTRERT, oebsOrdrenummer)
        assertThrows<IllegalArgumentException> {
            delbestillingStatusService.oppdaterStatus(delbestilling.saksnummer, Status.REGISTRERT, "123")
        }
    }

    @Test
    fun `skal oppdatere status på dellinjer`() = runTest {
        // Appen vil alltid kjøre med Europe/Oslo (ref. Dockerimage), men setter det eksplisitt her pga Github Runners bruker UTC
        TimeZone.setDefault(TIME_ZONE_EUROPE_OSLO)

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
        assertEquals(Status.DELVIS_SKIPNINGSBEKREFTET, delbestilling.status)
        assertEquals(DellinjeStatus.SKIPNINGSBEKREFTET, delbestilling.delbestilling.deler[0].status)
        assertEquals(datoOppdatert, delbestilling.delbestilling.deler[0].datoSkipningsbekreftet)
        assertEquals(LocalDate.of(2023, 10, 2), delbestilling.delbestilling.deler[0].forventetLeveringsdato)

        assertEquals(null, delbestilling.delbestilling.deler[1].status)
        assertEquals(null, delbestilling.delbestilling.deler[1].datoSkipningsbekreftet)
        assertEquals(null, delbestilling.delbestilling.deler[1].forventetLeveringsdato)

        // Skipningsbekreft andre del
        delbestillingStatusService.oppdaterDellinjeStatus(
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
        var delbestilling = transaction {
            delbestillingRepository.hentDelbestilling(oebsOrdrenummer)
        }
        assertNull(delbestilling)

        // Skal bare ignorere ukjent ordrenummer
        assertDoesNotThrow {
            delbestillingStatusService.oppdaterDellinjeStatus(
                oebsOrdrenummer,
                DellinjeStatus.SKIPNINGSBEKREFTET,
                "123456",
                LocalDate.now(),
            )
        }

        delbestilling = transaction {
            delbestillingRepository.hentDelbestilling(oebsOrdrenummer)
        }
        assertNull(delbestilling)
    }
}