package no.nav.hjelpemidler.delbestilling.delbestilling

import no.nav.hjelpemidler.delbestilling.common.DelUkjent
import no.nav.hjelpemidler.delbestilling.common.DellinjeUkjentDel
import no.nav.hjelpemidler.delbestilling.common.Levering
import no.nav.hjelpemidler.delbestilling.testdata.delLinje
import no.nav.hjelpemidler.delbestilling.testdata.delbestilling
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ManuellDelbestillingEpostTest {

    @Test
    fun `skal lage epost for manuell delbestilling uten personopplysninger`() {
        val delbestilling = delbestilling(
            deler = listOf(delLinje(hmsnr = "168802", navn = "Armlene høyre")),
        ).copy(
            serienr = null,
            brukernr = "12345",
            ukjenteDeler = listOf(
                DellinjeUkjentDel(
                    delUkjent = DelUkjent(
                        hmsnr = null,
                        levArtNr = "LEV-456",
                        beskrivelse = "Venstre armlene",
                    ),
                    antall = 2,
                )
            ),
            levering = Levering.TIL_SERVICE_OPPDRAG,
            epostTekniker = "tekniker@example.com",
        )

        val html = ManuellDelbestillingEpost(delbestilling).tilHtml()

        assertTrue(html.contains("<strong>HMS-nr.:</strong> 236958"))
        assertTrue(html.contains("236958"))
        assertTrue(html.contains("<strong>Brukernr.:</strong> 12345"))
        assertTrue(html.contains("12345"))
        assertFalse(html.contains("<strong>Serienr.:</strong>"))
        assertTrue(html.contains("HMS-nr. 168802<br>"))
        assertTrue(html.contains("Armlene høyre"))
        assertTrue(html.contains("Lev.art.nr. LEV-456<br>"))
        assertTrue(html.contains("Venstre armlene"))
        assertTrue(html.contains("2 stk."))
        assertTrue(html.contains("tekniker@example.com"))
        assertTrue(html.contains("Brukes i serviceoppdrag"))
        assertFalse(html.contains("Fødselsnummer"))
        assertFalse(html.contains("Folkeregistrert adresse"))
        assertFalse(html.contains("Hjelpemiddelbruker"))
    }

    @Test
    fun `skal vise serienummer og escape dynamisk innhold`() {
        val delbestilling = delbestilling(
            deler = emptyList(),
            serienr = "654321",
        ).copy(
            brukernr = null,
            navn = "Stol <modell>",
            ukjenteDeler = listOf(
                DellinjeUkjentDel(
                    delUkjent = DelUkjent(
                        hmsnr = "112233",
                        levArtNr = null,
                        beskrivelse = null,
                    ),
                    antall = 1,
                )
            ),
            epostTekniker = "tekniker@example.com",
        )

        val html = ManuellDelbestillingEpost(delbestilling).tilHtml()

        assertTrue(html.contains("<strong>Serienr.:</strong> 654321"))
        assertTrue(html.contains("654321"))
        assertFalse(html.contains("<strong>Brukernr.:</strong>"))
        assertTrue(html.contains("Stol &lt;modell&gt;"))
        assertFalse(html.contains("Stol <modell>"))
        assertTrue(html.contains("HMS-nr. 112233"))
        assertTrue(html.contains("<strong>Levering:</strong> Til XK-lager"))
    }
}