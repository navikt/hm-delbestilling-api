package no.nav.hjelpemidler.delbestilling.grunndata.requests

import com.fasterxml.jackson.databind.JsonNode
import no.nav.hjelpemidler.delbestilling.delbestilling.Hmsnr
import no.nav.hjelpemidler.delbestilling.jsonMapper

fun hmsArtNrRequest(hmsnr: Hmsnr): JsonNode {
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