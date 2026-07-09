package no.nav.hjelpemidler.delbestilling.infrastructure.kafka

import tools.jackson.databind.node.ObjectNode
import no.nav.hjelpemidler.delbestilling.infrastructure.jsonMapper
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Ordre
import java.time.LocalDateTime
import java.util.UUID

const val OPPRETT_MANUELL_DELBESTILLING_EVENT_NAME = "hm-OpprettManuellDelbestilling"

data class ManuellDelbestillingKafkaPayload(
    val eventId: UUID,
    val saksnummer: Long,
    val brukersFnr: String,
    val mottattTidspunkt: LocalDateTime,
) {
    val eventName: String = OPPRETT_MANUELL_DELBESTILLING_EVENT_NAME
}