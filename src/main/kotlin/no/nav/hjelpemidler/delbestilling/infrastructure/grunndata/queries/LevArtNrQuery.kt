package no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.queries

import no.nav.hjelpemidler.delbestilling.infrastructure.jsonMapper
import tools.jackson.databind.JsonNode

// supplierRef er mappet som keyword i grunndata, så term gir eksakt treff.
// Bygges som objekt (ikke strengmal) siden levArtNr er fritekst fra bruker.
fun levArtNrQuery(levArtNr: String): JsonNode = jsonMapper.valueToTree(
    mapOf(
        "query" to mapOf("term" to mapOf("supplierRef" to levArtNr)),
        "size" to 50,
    )
)
