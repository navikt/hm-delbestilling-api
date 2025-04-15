package no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.queries

import com.fasterxml.jackson.databind.JsonNode
import no.nav.hjelpemidler.delbestilling.infrastructure.jsonMapper
import java.util.UUID

fun compatibleWithQuery(seriesId: UUID, produktId: UUID): JsonNode {
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
                    "must": [
                        {
                            "match": {
                                "attributes.egnetForKommunalTekniker": "true"
                            }
                        },
                        {
                            "match": {
                                "sparePart": "true"
                            }
                        }
                    ],
                    "minimum_should_match": 1
                }
            },
            "size": "10000"
        }
    """.trimIndent()
    )
}