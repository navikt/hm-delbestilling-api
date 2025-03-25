package no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.queries

import com.fasterxml.jackson.databind.JsonNode
import no.nav.hjelpemidler.delbestilling.jsonMapper

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
                            "match": {
                                "sparePart": "true"
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