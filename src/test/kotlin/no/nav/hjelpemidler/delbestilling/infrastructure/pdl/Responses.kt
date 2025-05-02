package no.nav.hjelpemidler.delbestilling.infrastructure.pdl

import org.intellij.lang.annotations.Language

@Language("JSON")
fun pdlRespons(kommunenr: String) =
    """
{
  "data": {
    "hentPerson": {
      "bostedsadresse": [
        {
          "vegadresse": {
            "kommunenummer": "$kommunenr"
          }
        }
      ]
    }
  }
}
""".trimIndent()

@Language("JSON")
fun pdlPersonIkkeFunnet() =
    """
{
  "errors": [
  {
      "message": "Fant ikke person",
      "locations": [
        {
          "line": 2,
          "column": 5
        }
      ],
      "path": [
        "hentPerson"
      ],
      "extensions": {
        "code": "not_found",
        "classification": "ExecutionAborted"
      }
    }
  ],
  "data": {
    "hentPerson": null
  }
}
""".trimIndent()

@Language("JSON")
fun pdlKode6Respons(kommunenr: String) =
    """
{
  "data": {
    "hentPerson": {
      "bostedsadresse": [
        {
          "vegadresse": {
            "kommunenummer": "$kommunenr"
          }
        }
      ],
      "adressebeskyttelse": [
        {
          "gradering": "${Gradering.STRENGT_FORTROLIG}"
        }
      ]
    }
  }
}
""".trimIndent()

@Language("JSON")
fun pdlKode7Respons(kommunenr: String) =
    """
{
  "data": {
    "hentPerson": {
      "bostedsadresse": [
        {
          "vegadresse": {
            "kommunenummer": "$kommunenr"
          }
        }
      ],
      "adressebeskyttelse": [
        {
          "gradering": "${Gradering.FORTROLIG}"
        }
      ]
    }
  }
}
""".trimIndent()