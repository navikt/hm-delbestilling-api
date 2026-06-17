package no.nav.hjelpemidler.delbestilling.infrastructure.outbox

import java.util.UUID

data class OutboxMelding(
    val id: Long,
    val topic: String,
    val key: String,
    val eventName: String,
    val eventId: UUID,
    val payload: String,
    val attempts: Int,
    val alerted: Boolean,
)
