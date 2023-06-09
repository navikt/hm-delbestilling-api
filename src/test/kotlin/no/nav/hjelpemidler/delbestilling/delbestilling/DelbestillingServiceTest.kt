package no.nav.hjelpemidler.delbestilling.delbestilling

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import no.nav.hjelpemidler.delbestilling.MockException
import no.nav.hjelpemidler.delbestilling.TestDatabase
import no.nav.hjelpemidler.delbestilling.delbestillerRolle
import no.nav.hjelpemidler.delbestilling.delbestillingRequest
import no.nav.hjelpemidler.delbestilling.oebs.OebsService
import no.nav.hjelpemidler.delbestilling.pdl.PdlService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class DelbestillingServiceTest {

    private var ds = TestDatabase.testDataSource
    private val delbestillingRepository = DelbestillingRepository(ds)
    private val pdlService = mockk<PdlService>().apply {
        coEvery { hentKommunenummer(any()) } returns "1234"
        coEvery { hentPersonNavn(any(), any()) } returns "Turid Tekniker"
    }
    private val oebsService = mockk<OebsService>(relaxed = true)
    private val delbestillingService = DelbestillingService(ds, delbestillingRepository, pdlService, oebsService)

    val bestillerFnr = "123"

    @BeforeEach
    fun setup() {
        TestDatabase.cleanAndMigrate(ds)
    }

    @Test
    fun `opprettDelbestilling happy path`() = runTest {
        assertEquals(0, delbestillingService.hentDelbestillinger(bestillerFnr).size)

        delbestillingService.opprettDelbestilling(delbestillerRolle(), delbestillingRequest(), bestillerFnr)
        assertEquals(1, delbestillingService.hentDelbestillinger(bestillerFnr).size)
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
}