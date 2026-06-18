package no.nav.hjelpemidler.delbestilling.infrastructure.oebs

import tools.jackson.databind.node.ObjectNode
import no.nav.hjelpemidler.delbestilling.infrastructure.jsonMapper
import java.util.UUID

const val OPPRETT_DELBESTILLING_EVENT_NAME = "hm-OpprettDelbestilling"

fun byggOebsKafkaPayload(eventId: UUID, ordre: Ordre): String {
    val event = jsonMapper.valueToTree<ObjectNode>(ordre as Any)
        .put("eventName", OPPRETT_DELBESTILLING_EVENT_NAME)
        .put("eventId", eventId.toString())
    return jsonMapper.writeValueAsString(event)
}
