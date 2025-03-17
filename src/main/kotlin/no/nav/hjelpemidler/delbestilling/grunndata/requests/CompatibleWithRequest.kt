package no.nav.hjelpemidler.delbestilling.grunndata.requests

import com.fasterxml.jackson.databind.JsonNode
import no.nav.hjelpemidler.delbestilling.jsonMapper
import java.util.UUID

fun compatibleWithRequest(seriesId: UUID, produktId: UUID): JsonNode {
    return jsonMapper.readTree(
        """
        {
            "query": {
                "bool": {
                    "should": [
                        {
                            "match": {
                                "attributes.compatibleWith.seriesIds": "$seriesId"
                            }
                        },
                        {
                            "match": {
                                "attributes.compatibleWith.productIds": "$produktId"
                            }
                        }
                    ],
                    "minimum_should_match": 1
                },
                "bool": {
                    "must": {
                        { "match": { "attributes.egnetForKommunalTekniker": "true" } }
                    }
                }
            },
            "size": "10000"
        }
    """.trimIndent()
    )
}