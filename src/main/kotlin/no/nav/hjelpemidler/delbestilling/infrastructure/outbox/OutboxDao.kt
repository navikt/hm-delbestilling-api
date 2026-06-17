package no.nav.hjelpemidler.delbestilling.infrastructure.outbox

import no.nav.hjelpemidler.database.JdbcOperations
import java.time.LocalDateTime
import java.util.UUID

class OutboxDao(private val tx: JdbcOperations) {

    fun leggTil(
        topic: String,
        key: String,
        eventName: String,
        eventId: UUID,
        payload: String,
    ): Long = tx.updateAndReturnGeneratedKey(
        sql = """
            INSERT INTO outbox (topic, key, event_name, event_id, payload)
            VALUES (:topic, :key, :event_name, :event_id::uuid, :payload)
        """.trimIndent(),
        queryParameters = mapOf(
            "topic" to topic,
            "key" to key,
            "event_name" to eventName,
            "event_id" to eventId.toString(),
            "payload" to payload,
        ),
    )

    fun hentPending(limit: Int = 100): List<OutboxMelding> = tx.list(
        sql = """
            SELECT id, topic, key, event_name, event_id, payload, attempts, alerted
            FROM outbox
            WHERE status = 'PENDING'
            ORDER BY id
            LIMIT :limit
        """.trimIndent(),
        queryParameters = mapOf("limit" to limit),
    ) { row ->
        OutboxMelding(
            id = row.long("id"),
            topic = row.string("topic"),
            key = row.string("key"),
            eventName = row.string("event_name"),
            eventId = row.uuid("event_id"),
            payload = row.string("payload"),
            attempts = row.int("attempts"),
            alerted = row.boolean("alerted"),
        )
    }

    fun markerPublisert(id: Long) = tx.update(
        sql = """
            UPDATE outbox
            SET status = 'PUBLISHED', publisert = CURRENT_TIMESTAMP
            WHERE id = :id
        """.trimIndent(),
        queryParameters = mapOf("id" to id),
    )

    fun registrerFeil(id: Long, feil: String, skalVarsle: Boolean) = tx.update(
        sql = """
            UPDATE outbox
            SET attempts    = attempts + 1,
                last_error  = :last_error,
                alerted     = CASE WHEN :skal_varsle THEN true ELSE alerted END
            WHERE id = :id
        """.trimIndent(),
        queryParameters = mapOf(
            "id" to id,
            "last_error" to feil,
            "skal_varsle" to skalVarsle,
        ),
    )

    fun slettPubliserteEldreEnn(tidspunkt: LocalDateTime): Int = tx.update(
        sql = """
            DELETE FROM outbox
            WHERE status = 'PUBLISHED' AND publisert < :tidspunkt
        """.trimIndent(),
        queryParameters = mapOf("tidspunkt" to tidspunkt),
    ).actualRowCount
}
