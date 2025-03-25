package no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.queries

import com.fasterxml.jackson.databind.JsonNode
import no.nav.hjelpemidler.delbestilling.delbestilling.Hmsnr
import no.nav.hjelpemidler.delbestilling.jsonMapper

fun hmsArtNrQuery(hmsnr: Hmsnr): JsonNode {
    return jsonMapper.readTree(
        """
        {
            "query": {
                "bool": {
                    "must": [
                        {
                            "match": {
                                "hmsArtNr": "$hmsnr"
                            }
                        }
                    ]
                }
            },
            "size": "1"
        }
    """.trimIndent()
    )
}