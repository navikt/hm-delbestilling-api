package no.nav.hjelpemidler.delbestilling.infrastructure.kafka

import tools.jackson.databind.node.ObjectNode
import no.nav.hjelpemidler.delbestilling.infrastructure.jsonMapper
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.OPPRETT_DELBESTILLING_EVENT_NAME
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Ordre
import java.util.UUID

const val OPPRETT_MANUELL_DELBESTILLING_EVENT_NAME = "hm-OpprettManuellDelbestilling"

fun byggManuellDelbestillingKafkaPayload(eventId: UUID, ordre: Ordre): String {
    val event = jsonMapper.valueToTree<ObjectNode>(ordre as Any)
        .put("eventName", OPPRETT_DELBESTILLING_EVENT_NAME)
        .put("eventId", eventId.toString())
    return jsonMapper.writeValueAsString(event)
}