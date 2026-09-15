package no.nav.hjelpemidler.delbestilling.infrastructure.epostoutbox

import no.nav.hjelpemidler.delbestilling.infrastructure.email.ContentType
import no.nav.hjelpemidler.delbestilling.runWithTestContext
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals

class EpostOutboxDispatcherTest {

    @Test
    fun `skal sende pending epost én gang`() = runWithTestContext {
        // Legg en ventende e-post i databasen, slik en manuell delbestilling gjør.
        transaction {
            epostOutboxDao.leggTil(
                mottaker = "saksbehandler@nav.no",
                emne = "Delbestilling til manuell behandling",
                html = "<p>Test</p>",
            )
        }

        // Første kjøring sender e-posten og markerer outbox-raden som sendt.
        epostOutboxDispatcher.dispatchPending()
        // Neste kjøring skal ikke sende den samme e-posten på nytt.
        epostOutboxDispatcher.dispatchPending()

        assertEquals(1, emailClient.outbox.size)
        with(emailClient.outbox.single()) {
            assertEquals("saksbehandler@nav.no", recipent)
            assertEquals("Delbestilling til manuell behandling", subject)
            assertEquals("<p>Test</p>", body)
            assertEquals(ContentType.HTML, contentType)
        }
    }

    @Test
    fun `skal slette gamle sendte eposter men beholde pending eposter`() = runWithTestContext {
        transaction {
            epostOutboxDao.leggTil("saksbehandler@nav.no", "Gammel", "<p>Gammel</p>")
            delbestillingRepository.tx.update(
                sql = """
                    UPDATE epost_outbox
                    SET status = 'SENT', sendt = :sendt
                """.trimIndent(),
                queryParameters = mapOf("sendt" to LocalDateTime.now().minusDays(31)),
            )
            epostOutboxDao.leggTil("saksbehandler@nav.no", "Pending", "<p>Pending</p>")
        }

        EpostOutboxDispatcher(transaction, email, slack, clock).slettGamleSendteEposter(bevarDager = 30)

        val antallRader = transaction {
            delbestillingRepository.tx.single("SELECT COUNT(*) FROM epost_outbox") { row -> row.int(1) }
        }
        assertEquals(1, antallRader)
    }
}
