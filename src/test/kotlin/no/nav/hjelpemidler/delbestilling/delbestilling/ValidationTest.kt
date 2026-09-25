package no.nav.hjelpemidler.delbestilling.delbestilling

import no.nav.hjelpemidler.delbestilling.common.DellinjeUkjentDel
import no.nav.hjelpemidler.delbestilling.common.DelUkjent
import no.nav.hjelpemidler.delbestilling.oppslag.XkLagerRequest
import no.nav.hjelpemidler.delbestilling.testdata.delLinje
import no.nav.hjelpemidler.delbestilling.testdata.delbestilling
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
    fun `skal kreve enten serienr eller brukernr, ikke begge eller ingen`() {
        assertEquals(
            listOf("Brukernr eller serienr må være satt"),
            validateSerienrEllerBrukernr(serienr = null, brukernr = null),
        )
        assertEquals(
            listOf("Kan ikke inneholde både serienr. og brukernr"),
            validateSerienrEllerBrukernr(serienr = "123456", brukernr = "12345"),
        )
        assertEquals(emptyList(), validateSerienrEllerBrukernr(serienr = "123456", brukernr = null))
        assertEquals(emptyList(), validateSerienrEllerBrukernr(serienr = null, brukernr = "12345"))
    }

    @Test
    fun `skal behandle blank serienr og brukernr som ikke satt`() {
        assertEquals(
            listOf("Brukernr eller serienr må være satt"),
            validateSerienrEllerBrukernr(serienr = "", brukernr = ""),
        )
        assertEquals(emptyList(), validateSerienrEllerBrukernr(serienr = "", brukernr = "12345"))
        assertEquals(emptyList(), validateSerienrEllerBrukernr(serienr = "123456", brukernr = ""))
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
    fun `skal returnere alle feilmeldinger for serienr eller brukernr med feil lengde og ugyldige tegn`() {
        assertEquals(
            listOf("Serienr må ha 6 siffer", "Serienr skal kun bestå av tall"),
            validateSerienrEllerBrukernr(serienr = "12a", brukernr = null),
        )
        assertEquals(
            listOf("Brukernr må være 5-8 siffer", "Brukernr skal kun bestå av tall"),
            validateSerienrEllerBrukernr(serienr = null, brukernr = "12a"),
        )
    }

    @Test
    fun `skal validere XkLager-request`() {
        assertEquals(emptyList(), validateXkLagerRequest(XkLagerRequest("123456", "654321", null)))
        assertEquals(emptyList(), validateXkLagerRequest(XkLagerRequest("123456", "", "12345")))
        assertEquals(
            listOf("Hmsnr må ha 6 siffer"),
            validateXkLagerRequest(XkLagerRequest("12345", "654321", null)),
        )
        assertEquals(
            listOf("Brukernr eller serienr må være satt"),
            validateXkLagerRequest(XkLagerRequest("123456", null, null)),
        )
        assertEquals(
            listOf("Kan ikke inneholde både serienr. og brukernr"),
            validateXkLagerRequest(XkLagerRequest("123456", "654321", "12345")),
        )
    }

    @Test
    fun `skal bare sammenligne bestillinger med samme identifikator`() {
        val bestillingMedSerienr = delbestilling(serienr = "123456", brukerNr = null)
        val bestillingMedBrukernr = delbestilling(serienr = null, brukerNr = "12345")

        assertEquals(true, harSammeBestillingsidentifikator(bestillingMedSerienr, "123456", null))
        assertEquals(false, harSammeBestillingsidentifikator(bestillingMedSerienr, "654321", null))
        assertEquals(false, harSammeBestillingsidentifikator(bestillingMedBrukernr, "123456", null))
        assertEquals(true, harSammeBestillingsidentifikator(bestillingMedBrukernr, null, "12345"))
        assertEquals(false, harSammeBestillingsidentifikator(bestillingMedBrukernr, null, "54321"))
        assertEquals(false, harSammeBestillingsidentifikator(bestillingMedSerienr, null, "12345"))
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
    fun `skal avvise epost lengre enn 255 tegn for ukjente deler`() {
        val ukjenteDeler = listOf(ukjentDel(hmsnr = "123456"))
        val forLangEpost = "a".repeat(250) + "@nav.no"

        assertEquals(
            listOf("E-postadresse kan ikke være lengre enn 255 tegn"),
            validateUkjenteDeler(ukjenteDeler, forLangEpost),
        )
        assertEquals(
            emptyList(),
            validateUkjenteDeler(ukjenteDeler, "a".repeat(243) + "@nav.no"),
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
            listOf("HMS-nr for ukjent del må ha 6 siffer", "HMS-nr for ukjent del skal kun bestå av tall"),
            validateUkjentDel(ukjentDel(hmsnr = "12a")),
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