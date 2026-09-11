package no.nav.hjelpemidler.delbestilling.infrastructure.epostoutbox

import no.nav.hjelpemidler.database.JdbcOperations
import java.time.LocalDateTime

class EpostOutboxDao(private val tx: JdbcOperations) {
    fun leggTil(mottaker: String, emne: String, html: String) = tx.update(
        sql = """
            INSERT INTO epost_outbox (mottaker, emne, html)
            VALUES (:mottaker, :emne, :html)
        """.trimIndent(),
        queryParameters = mapOf("mottaker" to mottaker, "emne" to emne, "html" to html),
    )

    fun hentPending(limit: Int): List<EpostOutboxMelding> = tx.list(
        sql = """
            SELECT id, mottaker, emne, html, attempts, alerted
            FROM epost_outbox
            WHERE status = 'PENDING'
            ORDER BY id
            LIMIT :limit
        """.trimIndent(),
        queryParameters = mapOf("limit" to limit),
    ) { row ->
        EpostOutboxMelding(
            id = row.long("id"),
            mottaker = row.string("mottaker"),
            emne = row.string("emne"),
            html = row.string("html"),
            attempts = row.int("attempts"),
            alerted = row.boolean("alerted"),
        )
    }

    fun markerSendt(id: Long) = tx.update(
        sql = "UPDATE epost_outbox SET status = 'SENT', sendt = CURRENT_TIMESTAMP WHERE id = :id",
        queryParameters = mapOf("id" to id),
    )

    fun slettSendteEldreEnn(tidspunkt: LocalDateTime): Int = tx.update(
        sql = """
            DELETE FROM epost_outbox
            WHERE status = 'SENT' AND sendt < :tidspunkt
        """.trimIndent(),
        queryParameters = mapOf("tidspunkt" to tidspunkt),
    ).actualRowCount

    fun registrerFeil(id: Long, feil: String, skalVarsle: Boolean) = tx.update(
        sql = """
            UPDATE epost_outbox
            SET attempts = attempts + 1,
                last_error = :last_error,
                alerted = CASE WHEN :skal_varsle THEN true ELSE alerted END
            WHERE id = :id
        """.trimIndent(),
        queryParameters = mapOf("id" to id, "last_error" to feil, "skal_varsle" to skalVarsle),
    )
}