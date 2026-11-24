package no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.queries

import no.nav.hjelpemidler.delbestilling.infrastructure.jsonMapper
import tools.jackson.databind.JsonNode
import java.util.UUID

// Lev.art.nr er bare unikt per leverandør, så vi søker med begge. Begge feltene er keyword i grunndata.
// Bygges som objekt (ikke strengmal) siden levArtNr er fritekst fra bruker.
// size = 2 slik at vi kan oppdage (feilaktige) duplikater.
fun levArtNrQuery(levArtNr: String, leverandørId: UUID): JsonNode = jsonMapper.valueToTree(
    mapOf(
        "query" to mapOf(
            "bool" to mapOf(
                "filter" to listOf(
                    mapOf("term" to mapOf("supplierRef" to levArtNr)),
                    mapOf("term" to mapOf("supplier.id" to leverandørId.toString())),
                )
            )
        ),
        "size" to 2,
    )
)
