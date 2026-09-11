package no.nav.hjelpemidler.delbestilling.delbestilling

import no.nav.hjelpemidler.delbestilling.common.DellinjeUkjentDel
import no.nav.hjelpemidler.delbestilling.common.DelUkjent
import no.nav.hjelpemidler.delbestilling.testdata.delLinje
import no.nav.hjelpemidler.delbestilling.testdata.delbestillingRequest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class ValidationTest {

    @Test
    fun `skal returnere feilmelding når delbestilling mangler deler`() {
        val feilmeldinger = validateDelbestillingRequest(delbestillingRequest(deler = emptyList()))
        assertEquals("Delbestillingen må inneholde minst én dellinje", feilmeldinger.first())
    }

    @Test
    fun `skal kreve serienr eller brukernr`() {
        assertEquals(
            listOf("Brukernr eller serienr må være satt"),
            validateSerienrEllerBrukernr(serienr = null, brukernr = null),
        )
        assertEquals(emptyList(), validateSerienrEllerBrukernr(serienr = "123456", brukernr = null))
        assertEquals(emptyList(), validateSerienrEllerBrukernr(serienr = null, brukernr = "12345"))
    }

    @Test
    fun `skal ikke akseptere at både brukern og serienr er satt`() {

        assertEquals(
            emptyList(),
            validateKunEntenSerienrEllerBrukernr(serienr = "123456", brukernr = null),
        )
        assertEquals(
            emptyList(),
            validateKunEntenSerienrEllerBrukernr(serienr = null, brukernr = "12345"),
        )
        assertEquals(
            listOf("Kan ikke inneholde både serienr. og brukernr"),
            validateKunEntenSerienrEllerBrukernr(serienr = "123456", brukernr = "12345"),
        )
    }

    @Test
    fun `skal validere brukernr med fem til åtte siffer`() {
        assertEquals(emptyList(), validateSerienrEllerBrukernr(serienr = null, brukernr = "12345"))
        assertEquals(emptyList(), validateSerienrEllerBrukernr(serienr = null, brukernr = "12345678"))
        assertEquals(
            listOf("Brukernr må være 5-8 siffer"),
            validateSerienrEllerBrukernr(serienr = null, brukernr = "1234"),
        )
        assertEquals(
            listOf("Brukernr må være 5-8 siffer"),
            validateSerienrEllerBrukernr(serienr = null, brukernr = "123456789"),
        )
    }

    @Test
    fun `skal godta gyldige ukjente deler`() {
        val request = delbestillingRequest(deler = emptyList()).let {
            it.copy(
                delbestilling = it.delbestilling.copy(
                    ukjenteDeler = listOf(
                        ukjentDel(hmsnr = "123456"),
                        ukjentDel(levArtNr = "LEV-123", beskrivelse = "Venstre armlene"),
                    ),
                    epostTekniker = "tekniker@example.com",
                )
            )
        }

        assertEquals(emptyList(), validateDelbestillingRequest(request))
    }

    @Test
    fun `skal kreve gyldig epost for ukjente deler`() {
        val ukjenteDeler = listOf(ukjentDel(hmsnr = "123456"))

        assertEquals(
            listOf("Tekniker må oppgi en gyldig e-postadresse"),
            validateUkjenteDeler(ukjenteDeler, null),
        )
        assertEquals(
            listOf("Tekniker må oppgi en gyldig e-postadresse"),
            validateUkjenteDeler(ukjenteDeler, "ugyldig"),
        )
    }

    @Test
    fun `skal validere identifikator og antall for ukjent del`() {
        assertEquals(
            listOf("Ukjent del må ha HMS-nr eller leverandørens artikkelnummer"),
            validateUkjentDel(ukjentDel()),
        )
        assertEquals(
            listOf("HMS-nr for ukjent del må ha 6 siffer"),
            validateUkjentDel(ukjentDel(hmsnr = "12345")),
        )
        assertEquals(
            listOf("Leverandørens artikkelnummer må være 1-20 tegn"),
            validateUkjentDel(ukjentDel(levArtNr = " ")),
        )
        assertEquals(
            listOf("Antall for ukjent del må være minst 1"),
            validateUkjentDel(ukjentDel(hmsnr = "123456", antall = 0)),
        )
    }

    @Test
    fun `skal validere beskrivelse for leverandørnummer`() {
        assertEquals(
            listOf("Ukjent del med leverandørens artikkelnummer må ha en beskrivelse"),
            validateUkjentDel(ukjentDel(levArtNr = "LEV-123")),
        )
        assertEquals(
            listOf("Beskrivelse av ukjent del kan ikke være lengre enn 200 tegn"),
            validateUkjentDel(ukjentDel(levArtNr = "LEV-123", beskrivelse = "a".repeat(201))),
        )
    }

    @Test
    fun `skal returnere feilmelding når bestiller mangler opplæring på batteri`() {
        val requestMedOpplæring = delbestillingRequest(
            deler = listOf(
                delLinje(antall = 1, kategori = "Batteri")
            ),
            harOpplæringPåBatteri = true
        )
        assertEquals(0, validateDelbestillingRequest(requestMedOpplæring).size)

        val requestUtenOpplæring = delbestillingRequest(
            deler = listOf(
                delLinje(antall = 1, kategori = "Batteri")
            ),
            harOpplæringPåBatteri = false
        )
        assertEquals(
            "Tekniker må bekrefte opplæring i bytting av batteriene",
            validateDelbestillingRequest(requestUtenOpplæring).first()
        )
    }
}

private fun ukjentDel(
    hmsnr: String? = null,
    levArtNr: String? = null,
    beskrivelse: String? = null,
    antall: Int = 1,
) = DellinjeUkjentDel(
    delUkjent = DelUkjent(hmsnr = hmsnr, levArtNr = levArtNr, beskrivelse = beskrivelse),
    antall = antall,
)