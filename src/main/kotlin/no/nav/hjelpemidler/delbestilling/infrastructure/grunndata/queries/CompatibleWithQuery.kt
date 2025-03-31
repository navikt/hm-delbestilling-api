package no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.queries

import org.intellij.lang.annotations.Language
import java.util.UUID

@Language("JSON")
fun compatibleWithQuery(seriesId: UUID, produktId: UUID) =
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