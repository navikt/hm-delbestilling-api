package no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.queries

import tools.jackson.databind.JsonNode
import no.nav.hjelpemidler.delbestilling.infrastructure.jsonMapper

fun alleDelerSomKanBestillesQuery(): JsonNode {
    return jsonMapper.readTree(
        """
        {
            "query": {
                "bool": {
                    "must": [
                        {
                            "match": {
                                "attributes.egnetForKommunalTekniker": "true"
                            }
                        },
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
                    ]
                }
            },
            "size": "10000"
        }
    """.trimIndent()
    )
}