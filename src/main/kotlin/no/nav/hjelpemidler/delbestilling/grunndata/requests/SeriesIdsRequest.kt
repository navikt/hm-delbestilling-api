package no.nav.hjelpemidler.delbestilling.grunndata.requests

import com.fasterxml.jackson.databind.JsonNode
import no.nav.hjelpemidler.delbestilling.jsonMapper
import java.util.UUID

fun seriesIdsRequest(seriesId: UUID): JsonNode {
    return jsonMapper.readTree(
        """
        {
            "query": {
                "bool": {
                    "must": [
                        {
                            "match": {
                                "attributes.compatibleWith.seriesIds": "$seriesId"
                            }
                        }
                    ]
                }
            },
            "size": "10000"
        }
    """.trimIndent()
    )
}