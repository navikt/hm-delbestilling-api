package no.nav.hjelpemidler.delbestilling.grunndata.requests

import com.fasterxml.jackson.databind.JsonNode
import no.nav.hjelpemidler.delbestilling.jsonMapper
import java.util.UUID

fun alleHjmMedIdEllerSeriesIdRequest(seriesIds: Set<UUID>, produktIds: Set<UUID>): JsonNode {
    return jsonMapper.readTree(
        """
        {
            "query": {
                "bool": {
                    "should": [
                        {
                            "terms": {
                                "seriesId": [${seriesIds.joinToString(separator = ","){ "\"$it\"" }}]
                            }
                        },
                        {
                            "terms": {
                                "id": [${produktIds.joinToString(separator = ","){ "\"$it\"" }}]
                            }
                        }
                    ],
                    "must": [
                        {
                            "match": {
                                "main": "true"
                            }
                        }
                    ],
                    "minimum_should_match": 1
                }
            },
            "size": 10000
        }
    """.trimIndent()
    )
}
