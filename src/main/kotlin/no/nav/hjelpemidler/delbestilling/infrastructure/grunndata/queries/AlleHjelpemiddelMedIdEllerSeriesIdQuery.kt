package no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.queries

import org.intellij.lang.annotations.Language
import java.util.UUID

@Language("JSON")
fun alleHjelpemiddelMedIdEllerSeriesIdQuery(seriesIds: Set<UUID>, produktIds: Set<UUID>) =
"""
{
    "query": {
        "bool": {
            "should": [
                {
                    "terms": {
                        "seriesId": [${seriesIds.joinToString(separator = ",") { "\"$it\"" }}]
                    }
                },
                {
                    "terms": {
                        "id": [${produktIds.joinToString(separator = ",") { "\"$it\"" }}]
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
