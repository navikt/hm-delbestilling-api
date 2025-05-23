package no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction

import kotlinx.coroutines.test.runTest
import no.nav.hjelpemidler.delbestilling.delbestilling.BestillerType
import no.nav.hjelpemidler.delbestilling.testdata.TestDatabase
import no.nav.hjelpemidler.delbestilling.testdata.Testdata
import no.nav.hjelpemidler.delbestilling.testdata.delbestilling
import no.nav.hjelpemidler.delbestilling.testdata.organisasjon
import kotlin.test.Test
import kotlin.test.assertEquals

import kotlin.test.assertFailsWith

class TransactionTest {

    private var ds = TestDatabase.cleanAndMigrate(TestDatabase.testDataSource)
    private val transaction = Transaction(ds, TransactionScopeFactory())

    @Test
    fun `skal rulle tilbake dersom feil oppstår i transaction`() = runTest {
        assertFailsWith<RuntimeException> {
            transaction(returnGeneratedKeys = true) {
                lagreTestBestilling()
                throw RuntimeException("Noe gikk galt.")
            }
        }
        transaction {
            assertEquals(0, delbestillingRepository.hentDelbestillinger(Testdata.defaultFnr).size)
        }
    }

    @Test
    fun `skal rulle tilbake dersom indre transaction feiler`() = runTest {
        assertFailsWith<RuntimeException> {
            transaction(returnGeneratedKeys = true) {
                lagreTestBestilling()
                transaction {
                    throw RuntimeException("Noe gikk galt.")
                }
            }
        }
        transaction {
            assertEquals(0, delbestillingRepository.hentDelbestillinger(Testdata.defaultFnr).size)
        }
    }

    @Test
    fun `skal rulle tilbake dersom ytre transaction feiler`() = runTest {
        assertFailsWith<RuntimeException> {
            transaction(returnGeneratedKeys = true) {
                transaction {
                    lagreTestBestilling()
                }
                throw RuntimeException("Noe gikk galt.")
            }
        }
        transaction {
            assertEquals(0, delbestillingRepository.hentDelbestillinger(Testdata.defaultFnr).size)
        }
    }

    private fun TransactionScope.lagreTestBestilling() {
        delbestillingRepository.lagreDelbestilling(
            bestillerFnr = Testdata.defaultFnr,
            brukerFnr = Testdata.defaultFnr,
            brukerKommunenr = Testdata.defaultKommunenummer,
            delbestilling = delbestilling(),
            brukersKommunenavn = "Oslo",
            bestillersOrganisasjon = organisasjon(),
            bestillerType = BestillerType.KOMMUNAL,
        )
    }
}