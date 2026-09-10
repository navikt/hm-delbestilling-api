package no.nav.hjelpemidler.delbestilling.delbestilling

import no.nav.hjelpemidler.delbestilling.common.DelUkjent
import no.nav.hjelpemidler.delbestilling.common.DellinjeUkjentDel
import no.nav.hjelpemidler.delbestilling.common.Levering
import no.nav.hjelpemidler.delbestilling.testdata.delLinje
import no.nav.hjelpemidler.delbestilling.testdata.delbestilling
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertTrue

class ManuellEpostTestManuellDelbestilling {

    @Test
    fun `lag lokal forhåndsvisning av epost`() {
        val delbestilling = delbestilling(
            deler = listOf(
                delLinje(
                    hmsnr = "123456",
                    navn = "En helt super del",
                ),
                delLinje(
                    hmsnr = "345678",
                    navn = "Enda en helt super del",
                ),
            ),
        ).copy(
            hmsnr = "999999",
            serienr = null,
            brukernr = "12345678",
            navn = "Et helt supert hjelpemiddel",
            ukjenteDeler = listOf(
                DellinjeUkjentDel(
                    delUkjent = DelUkjent(
                        hmsnr = null,
                        levArtNr = "456",
                        beskrivelse = "Venstre armlene med feste",
                    ),
                    antall = 1,
                )
            ),
            levering = Levering.TIL_XK_LAGER,
            epostTekniker = "tekniker@nav.no",
        )

        val html = ManuellDelbestillingEpost(delbestilling).tilHtml()
        val preview = Path.of("build", "manual-email-preview.html").toAbsolutePath()

        Files.createDirectories(preview.parent)
        Files.writeString(preview, html)

        assertTrue(Files.exists(preview))
        println("Lokal e-postvisning: $preview")
    }
}