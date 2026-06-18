package no.nav.hjelpemidler.delbestilling.testdata.fixtures

import no.nav.hjelpemidler.delbestilling.TestContext
import no.nav.hjelpemidler.delbestilling.infrastructure.jsonMapper
import no.nav.hjelpemidler.delbestilling.infrastructure.kafka.SOKNADSBEHANDLING_TOPIC
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.OPPRETT_DELBESTILLING_EVENT_NAME
import no.nav.hjelpemidler.delbestilling.infrastructure.outbox.OutboxMelding
import java.util.UUID

suspend fun TestContext.hentPendingOutbox(): List<OutboxMelding> = transaction {
    outboxDao.hentPending()
}

suspend fun TestContext.hentAntallOutboxRader(): Int = transaction {
    delbestillingRepository.tx.single(sql = "SELECT COUNT(*) FROM outbox") { row -> row.int(1) }
}

/**
 * Setter inn en outbox-rad direkte, uten å gå via opprettDelbestilling.
 * Nyttig i tester som vil kontrollere outbox-tilstand selv (f.eks. OutboxDispatcherTest).
 */
suspend fun TestContext.leggTilOutboxRad(
    topic: String = SOKNADSBEHANDLING_TOPIC,
    key: String = "1",
    eventName: String = OPPRETT_DELBESTILLING_EVENT_NAME,
    payload: String = """{"eventName":"$eventName","artikler":[{"hmsnr":"010101","antall":2}]}""",
) {
    transaction(returnGeneratedKeys = true) {
        outboxDao.leggTil(
            topic = topic,
            key = key,
            eventName = eventName,
            eventId = UUID.randomUUID(),
            payload = payload,
        )
    }
}

/** Dispatcher alle ventende outbox-meldinger til Kafka. */
suspend fun TestContext.dispatchOutbox() {
    outboxDispatcher.dispatchPending()
}

/**
 * Simulerer at OEBS behandler Kafka-meldinger publisert etter [antallFørDispatch].
 * Filtrerer på eventName slik at OEBS kun behandler relevante hendelser på den delte topic-en.
 */
fun TestContext.simulerKafkaBehandling(antallFørDispatch: Int = 0) {
    kafka.publiserte
        .drop(antallFørDispatch)
        .filter { it.eventName == OPPRETT_DELBESTILLING_EVENT_NAME }
        .forEach { melding ->
            val artikler = jsonMapper.readTree(melding.payload)["artikler"] ?: return@forEach
            artikler.forEach { artikkel ->
                oebslager.reduser(artikkel["hmsnr"].asText(), artikkel["antall"].asInt())
            }
        }
}

/** Dispatcher outbox og simulerer at OEBS behandler de publiserte meldingene. */
suspend fun TestContext.flushOutbox() {
    val antallFørDispatch = kafka.publiserte.size
    dispatchOutbox()
    simulerKafkaBehandling(antallFørDispatch)
}
