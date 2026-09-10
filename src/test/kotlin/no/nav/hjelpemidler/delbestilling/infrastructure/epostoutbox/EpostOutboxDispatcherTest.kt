package no.nav.hjelpemidler.delbestilling.infrastructure.epostoutbox

import no.nav.hjelpemidler.delbestilling.infrastructure.email.ContentType
import no.nav.hjelpemidler.delbestilling.runWithTestContext
import org.junit.jupiter.api.Test
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
}
