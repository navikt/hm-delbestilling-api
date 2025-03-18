package no.nav.hjelpemidler.delbestilling.grunndata.requests

import com.fasterxml.jackson.databind.JsonNode
import no.nav.hjelpemidler.delbestilling.jsonMapper
import java.util.UUID

fun alleDelerSomKanBestillesRequest(): JsonNode {
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