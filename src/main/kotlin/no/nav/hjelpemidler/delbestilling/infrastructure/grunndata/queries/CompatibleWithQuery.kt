package no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.queries

import no.nav.hjelpemidler.delbestilling.config.isDev
import tools.jackson.databind.JsonNode
import no.nav.hjelpemidler.delbestilling.infrastructure.jsonMapper
import java.util.UUID

fun compatibleWithQuery(seriesId: UUID, produktId: UUID): JsonNode {
    val tilgjengeligForTekniker = if (isDev()) "" else """                        {
                            "match": {
                                "attributes.egnetForKommunalTekniker": "true"
                            }
                        },"""
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
                    $tilgjengeligForTekniker
                        {
                            "bool": {
                                "should": [
                                    {
                                        "match": {
                                            "sparePart": "true"
                                        }
                                    },
                                    {
                                        "match": {
                                            "accessory": "true"
                                        }
                                    }
                                ],
                                "minimum_should_match": 1
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