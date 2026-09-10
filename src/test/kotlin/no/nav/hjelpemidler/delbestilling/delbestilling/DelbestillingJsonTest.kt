package no.nav.hjelpemidler.delbestilling.delbestilling

import no.nav.hjelpemidler.delbestilling.common.Delbestilling
import no.nav.hjelpemidler.delbestilling.infrastructure.jsonMapper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DelbestillingJsonTest {

    @Test
    fun `skal lese epost og beskrivelse fra delbestillingsrequest`() {
        val request = jsonMapper.readValue(
            """
            {
              "delbestilling": {
                "id": "218e3798-9c12-45dd-b3c2-d7eca5ffcb0c",
                "hmsnr": "123456",
                "serienr": null,
                "brukernr": "12345",
                "deler": [],
                "ukjenteDeler": [
                  {
                    "delUkjent": {
                      "hmsnr": null,
                      "levArtNr": "123",
                      "beskrivelse": "Skjerm"
                    },
                    "antall": 1
                  }
                ],
                "levering": "TIL_SERVICE_OPPDRAG",
                "harOpplæringPåBatteri": null,
                "navn": "Testhjelpemiddel",
                "epostTekniker": "tekniker@example.com"
              }
            }
            """.trimIndent(),
            DelbestillingRequest::class.java,
        )

        assertEquals("tekniker@example.com", request.delbestilling.epostTekniker)
        assertEquals("123", request.delbestilling.ukjenteDeler.single().delUkjent.levArtNr)
        assertEquals("Skjerm", request.delbestilling.ukjenteDeler.single().delUkjent.beskrivelse)

        val json = jsonMapper.writeValueAsString(request)
        assertTrue(json.contains("\"levArtNr\":\"123\""))
        assertFalse(json.contains("\"levArtnr\""))
    }

    @Test
    fun `skal lese eldre delbestilling uten epost og beskrivelse`() {
        val delbestilling = jsonMapper.readValue(
            """
            {
              "id": "218e3798-9c12-45dd-b3c2-d7eca5ffcb0c",
              "hmsnr": "123456",
              "serienr": "654321",
              "deler": [],
              "ukjenteDeler": [
                {
                  "delUkjent": {
                    "hmsnr": "112233",
                    "levArtnr": "123"
                  },
                  "antall": 1
                }
              ],
              "levering": "TIL_SERVICE_OPPDRAG",
              "harOpplæringPåBatteri": null,
              "navn": "Testhjelpemiddel"
            }
            """.trimIndent(),
            Delbestilling::class.java,
        )

        assertNull(delbestilling.epostTekniker)
        assertEquals("123", delbestilling.ukjenteDeler.single().delUkjent.levArtNr)
        assertNull(delbestilling.ukjenteDeler.single().delUkjent.beskrivelse)
    }
}