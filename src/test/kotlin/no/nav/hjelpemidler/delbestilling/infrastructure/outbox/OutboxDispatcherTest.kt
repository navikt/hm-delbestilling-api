package no.nav.hjelpemidler.delbestilling.infrastructure.outbox

import no.nav.hjelpemidler.delbestilling.fakes.FaultInjectingTransactional
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.OPPRETT_DELBESTILLING_EVENT_NAME
import no.nav.hjelpemidler.delbestilling.infrastructure.kafka.SOKNADSBEHANDLING_TOPIC
import no.nav.hjelpemidler.delbestilling.runWithTestContext
import no.nav.hjelpemidler.delbestilling.testdata.fixtures.hentPendingOutbox
import no.nav.hjelpemidler.delbestilling.testdata.fixtures.leggTilOutboxRad
import no.nav.hjelpemidler.delbestilling.testdata.fixtures.opprettDelbestilling
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class OutboxDispatcherTest {

    @Test
    fun `opprettDelbestilling skriver og dispatcher outbox-rad`() = runWithTestContext {
        assertTrue(kafka.publiserte.isEmpty())

        opprettDelbestilling()

        // opprettDelbestilling flushes outbox automatisk — meldingen skal være publisert til Kafka
        assertEquals(1, kafka.publiserte.size)
        with(kafka.publiserte.first()) {
            assertEquals(SOKNADSBEHANDLING_TOPIC, topic)
            assertTrue(payload.contains(OPPRETT_DELBESTILLING_EVENT_NAME))
        }
    }

    @Test
    fun `dispatcher publiserer pending meldinger og markerer dem som PUBLISHED`() = runWithTestContext {
        leggTilOutboxRad()
        assertEquals(1, hentPendingOutbox().size)

        outboxDispatcher.dispatchPending()

        assertEquals(0, hentPendingOutbox().size)
        assertEquals(1, kafka.publiserte.size)
        assertEquals(SOKNADSBEHANDLING_TOPIC, kafka.publiserte.first().topic)
    }

    @Test
    fun `dispatcher håndterer publiseringsfeil og øker attempts`() = runWithTestContext {
        leggTilOutboxRad()

        kafka.skalKasteFeil = true
        outboxDispatcher.dispatchPending()
        kafka.skalKasteFeil = false

        val pending = hentPendingOutbox()
        assertEquals(1, pending.size)
        assertEquals(1, pending.first().attempts)
        assertEquals(0, kafka.publiserte.size)
    }

    @Test
    fun `dispatcher varsler Slack etter terskel for antall feil`() = runWithTestContext {
        leggTilOutboxRad()
        kafka.skalKasteFeil = true

        repeat(5) { outboxDispatcher.dispatchPending() }

        io.mockk.verify(exactly = 1) {
            slack.varsleOmOutboxFeil(any(), any(), any())
        }
    }

    @Test
    fun `dispatcher varsler kun én gang selv om feilene fortsetter`() = runWithTestContext {
        leggTilOutboxRad()
        kafka.skalKasteFeil = true

        repeat(10) { outboxDispatcher.dispatchPending() }

        io.mockk.verify(exactly = 1) {
            slack.varsleOmOutboxFeil(any(), any(), any())
        }
    }

    @Test
    fun `outbox-rad opprettes ikke ved rollback av transaksjon`() = runWithTestContext {
        try {
            transaction(returnGeneratedKeys = true) {
                outboxDao.leggTil(
                    topic = SOKNADSBEHANDLING_TOPIC,
                    key = "1",
                    eventName = OPPRETT_DELBESTILLING_EVENT_NAME,
                    eventId = UUID.randomUUID(),
                    payload = "{}",
                )
                throw RuntimeException("Simulert feil som ruller tilbake tx")
            }
        } catch (e: Exception) {
            // forventet
        }

        assertEquals(0, hentPendingOutbox().size)
    }

    @Test
    fun `DB-feil i markerPublisert øker ikke attempts og varsler ikke Slack`() = runWithTestContext {
        leggTilOutboxRad()

        // Kall 1: hentPending — OK
        // Kall 2: markerPublisert — kast feil
        val faultInjecting = FaultInjectingTransactional(transaction).apply { kastFeilPåKall = 2 }
        val dispatcher = OutboxDispatcher(faultInjecting, kafka, slack)
        dispatcher.dispatchPending()

        // Kafka-publisering lyktes
        assertEquals(1, kafka.publiserte.size)

        // attempts er ikke økt — feilen ble ikke tolket som Kafka-feil
        assertEquals(0, hentPendingOutbox().first().attempts)

        // Ingen Slack-varsling
        io.mockk.verify(exactly = 0) { slack.varsleOmOutboxFeil(any(), any(), any()) }
    }
}
