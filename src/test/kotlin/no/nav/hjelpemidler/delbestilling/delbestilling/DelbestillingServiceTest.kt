package no.nav.hjelpemidler.delbestilling.delbestilling

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import no.nav.hjelpemidler.delbestilling.common.Lager
import no.nav.hjelpemidler.delbestilling.testdata.MockException
import no.nav.hjelpemidler.delbestilling.testdata.TestDatabase
import no.nav.hjelpemidler.delbestilling.testdata.delLinje
import no.nav.hjelpemidler.delbestilling.testdata.delbestillerRolle
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.AnmodningService
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.lagerstatus
import no.nav.hjelpemidler.delbestilling.testdata.delbestillingRequest
import no.nav.hjelpemidler.delbestilling.infrastructure.geografi.Kommuneoppslag
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Oebs
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.OebsPersoninfo
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.Pdl
import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction.Transaction
import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction.TransactionScopeFactory
import no.nav.hjelpemidler.delbestilling.infrastructure.slack.Slack
import no.nav.hjelpemidler.delbestilling.infrastructure.norg.Norg
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class DelbestillingServiceTest {

    val brukersFnr = "26928698180"
    val bestillerFnr = "13820599335"
    val teknikerNavn = "Turid Tekniker"
    val brukersKommunenr = "1234"

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

    @BeforeEach
    fun setup() {
        TestDatabase.cleanAndMigratedDataSource(ds)
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
        coEvery { oebs.sendDelbestilling(any(), any(), any()) } throws MockException("Kafka er nede")
        assertEquals(0, delbestillingService.hentDelbestillinger(bestillerFnr).size)
        assertThrows<MockException> {
            delbestillingService.opprettDelbestilling(delbestillingRequest(), bestillerFnr, delbestillerRolle())
        }
        assertEquals(0, delbestillingService.hentDelbestillinger(bestillerFnr).size)
    }

    @Test
    fun `skal feile dersom PDL og OEBS sine kommunenr er ulike for bruker`() = runTest {
        coEvery { oebs.hentPersoninfo(any()) } returns listOf(OebsPersoninfo("0000"))
        val resultat = delbestillingService
            .opprettDelbestilling(delbestillingRequest(), bestillerFnr, delbestillerRolle())
        assertEquals(DelbestillingFeil.ULIK_ADRESSE_PDL_OEBS, resultat.feil)
    }

    @Test
    fun `skal lagre anmodningsbehov ved ny delbestilling`() = runTest {
        val lager = Lager.OSLO
            coEvery { oebs.finnLagerenhet(any()) } returns lager
        val anmodningService =
            AnmodningService(
                transaction,
                oebs,
                mockk(relaxed = true),
                mockk(relaxed = true),
                mockk(relaxed = true)
            )
        val delbestillingService =
            DelbestillingService(
                transaction,
                pdl,
                oebs,
                kommuneoppslag,
                mockk(relaxed = true),
                slack,
                anmodningService,
            )

        val hmsnrEtterfylt = "111111"
        val hmsnrMedDekning = "222222"
        val hmsnrIkkeDekning = "333333"

        coEvery { oebs.hentLagerstatusForKommunenummer(any(), any()) } returns listOf(
            lagerstatus(
                antall = 2,
                hmsnr = hmsnrEtterfylt
            ),
            lagerstatus(
                antall = 2,
                hmsnr = hmsnrMedDekning
            ),
            lagerstatus(
                antall = 2,
                hmsnr = hmsnrIkkeDekning
            )
        )
        delbestillingService.opprettDelbestilling(
            delbestillingRequest(
                deler = listOf(
                    delLinje(antall = 3, hmsnr = hmsnrEtterfylt),
                    delLinje(antall = 2, hmsnr = hmsnrMedDekning),
                    delLinje(antall = 4, hmsnr = hmsnrIkkeDekning)
                )
            ),
            bestillerFnr,
            delbestillerRolle()
        )

        var delerTilRapportering = transaction { delUtenDekningDao.hentDelerTilRapportering(lager.nummer) }
        assertEquals(2, delerTilRapportering.size)
        assertEquals(1, delerTilRapportering.find { it.hmsnr == hmsnrEtterfylt }!!.antall)
        assertEquals(2, delerTilRapportering.find { it.hmsnr == hmsnrIkkeDekning }!!.antall)

        coEvery { oebs.hentLagerstatusForEnhet(any(), any()) } returns listOf(
            lagerstatus(
                antall = 0, // Påfyll i løpet av dagen
                hmsnr = hmsnrEtterfylt
            ),
            lagerstatus(
                antall = -2,
                hmsnr = hmsnrIkkeDekning
            )
        )
        val anmodninger = delbestillingService.rapporterDelerTilAnmodning().first()
        assertEquals(
            0,
            transaction { delUtenDekningDao.hentDelerTilRapportering(lager.nummer).size },
            "Det skal ikke lenger eksistere deler til rapportering"
        )
        assertEquals(1, anmodninger.anmodningsbehov.size)
        assertEquals(2, anmodninger.anmodningsbehov.find { it.hmsnr == hmsnrIkkeDekning }!!.antallSomMåAnmodes)

        delerTilRapportering = transaction { delUtenDekningDao.hentDelerTilRapportering(lager.nummer) }
        assertEquals(0, delerTilRapportering.size)
    }

    @Test
    fun `skal summere anmodningsbehov`() = runTest {
        val lager = Lager.OSLO
        val hmsnr1 = "111111"
        val hmsnr2 = "222222"
        coEvery { oebs.finnLagerenhet(any()) } returns lager
        val anmodningService =
            AnmodningService(
                transaction,
                oebs,
                mockk(relaxed = true),
                mockk(relaxed = true),
                mockk(relaxed = true)
            )
        val delbestillingService =
            DelbestillingService(
                transaction,
                pdl,
                oebs,
                kommuneoppslag,
                mockk(relaxed = true),
                slack,
                anmodningService,
            )

        // Første delbestilling
        coEvery { oebs.hentLagerstatusForKommunenummer(any(), any()) } returns listOf(
            lagerstatus(antall = 3, hmsnr = hmsnr1),
            lagerstatus(antall = 1, hmsnr = hmsnr2)
        )
        delbestillingService.opprettDelbestilling(
            delbestillingRequest(
                deler = listOf(
                    delLinje(antall = 3, hmsnr = hmsnr1),
                    delLinje(antall = 2, hmsnr = hmsnr2)
                )
            ),
            bestillerFnr,
            delbestillerRolle()
        )

        // Andre delbestilling
        coEvery { oebs.hentLagerstatusForKommunenummer(any(), any()) } returns listOf(
            lagerstatus(antall = 0, hmsnr = hmsnr1),
            lagerstatus(antall = 0, hmsnr = hmsnr2)
        )
        delbestillingService.opprettDelbestilling(
            delbestillingRequest(
                deler = listOf(
                    delLinje(antall = 2, hmsnr = hmsnr1),
                    delLinje(antall = 2, hmsnr = hmsnr2)
                )
            ),
            bestillerFnr,
            delbestillerRolle()
        )

        // Sjekk at anmodningsbehov er summert for begge delbestillingene
        val delerTilRapportering = transaction { delUtenDekningDao.hentDelerTilRapportering(lager.nummer) }
        assertEquals(2, delerTilRapportering.size)
        assertEquals(2, delerTilRapportering.find { it.hmsnr == hmsnr1 }!!.antall)
        assertEquals(3, delerTilRapportering.find { it.hmsnr == hmsnr2 }!!.antall)
    }
}
