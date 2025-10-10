package no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction

import no.nav.hjelpemidler.delbestilling.testdata.Testdata
import no.nav.hjelpemidler.delbestilling.testdata.fixtures.gittDelbestilling
import no.nav.hjelpemidler.delbestilling.testdata.runWithTestContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TransactionTest {

    @Test
    fun `happy path`() = runWithTestContext {
        transaction(returnGeneratedKeys = true) {
            gittDelbestilling()
        }
        transaction {
            assertEquals(1, delbestillingRepository.hentDelbestillinger(Testdata.fnr).size)
        }
    }

    @Test
    fun `skal rulle tilbake dersom feil oppstår i transaction`() = runWithTestContext {
        assertFailsWith<RuntimeException> {
            transaction(returnGeneratedKeys = true) {
                gittDelbestilling()
                throw RuntimeException("Noe gikk galt.")
            }
        }
        transaction {
            assertEquals(0, delbestillingRepository.hentDelbestillinger(Testdata.fnr).size)
        }
    }

    @Test
    fun `skal rulle tilbake dersom indre transaction feiler`() = runWithTestContext {
        assertFailsWith<RuntimeException> {
            transaction(returnGeneratedKeys = true) {
                gittDelbestilling()
                transaction {
                    throw RuntimeException("Noe gikk galt.")
                }
            }
        }
        transaction {
            assertEquals(0, delbestillingRepository.hentDelbestillinger(Testdata.fnr).size)
        }
    }

    @Test
    fun `skal rulle tilbake dersom ytre transaction feiler`() = runWithTestContext {
        assertFailsWith<RuntimeException> {
            transaction(returnGeneratedKeys = true) {
                transaction {
                    gittDelbestilling()
                }
                throw RuntimeException("Noe gikk galt.")
            }
        }
        transaction {
            assertEquals(0, delbestillingRepository.hentDelbestillinger(Testdata.fnr).size)
        }
    }
}