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
    fun `skal lage epost for manuell delbestilling uten personopplysninger med brukernr`() {
        val delbestilling = delbestilling(
            deler = listOf(delLinje(hmsnr = "168802", navn = "Armlene høyre")),
        ).copy(
            serienr = null,
            brukernr = "12345",
            ukjenteDeler = listOf(
                DellinjeUkjentDel(
                    delUkjent = DelUkjent(
                        hmsnr = null,
                        levArtNr = "456",
                        beskrivelse = "Venstre armlene",
                    ),
                    antall = 2,
                )
            ),
            levering = Levering.TIL_SERVICE_OPPDRAG,
            epostTekniker = "tekniker@nav.no",
        )

        val html = ManuellDelbestillingEpost(delbestilling, 19).tilHtml()

        assertTrue(html.contains("HMS-nr.:</strong> 236958") && html.contains("Brukernr.:</strong> 12345"))
        assertFalse(html.contains("<strong>Serienr.:</strong>"))
        assertTrue(
            html.contains("HMS-nr. 168802") &&
                html.contains("Armlene høyre") &&
                html.contains("Lev.art.nr. 456") &&
                html.contains("Venstre armlene") &&
                html.contains("2 stk.")
        )
        assertTrue(html.contains("tekniker@nav.no") && html.contains("Brukes i serviceoppdrag"))
        assertFalse(html.contains("Fødselsnummer"))
        assertFalse(html.contains("Folkeregistrert adresse"))
        assertFalse(html.contains("Hjelpemiddelbruker"))
    }

}