package no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.queries

import no.nav.hjelpemidler.delbestilling.delbestilling.Hmsnr
import org.intellij.lang.annotations.Language

@Language("JSON")
fun hmsArtNrQuery(hmsnr: Hmsnr) =
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