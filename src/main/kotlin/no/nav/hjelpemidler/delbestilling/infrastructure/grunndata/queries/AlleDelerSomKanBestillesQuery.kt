package no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.queries

import org.intellij.lang.annotations.Language

@Language("JSON")
fun alleDelerSomKanBestillesQuery() =
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
