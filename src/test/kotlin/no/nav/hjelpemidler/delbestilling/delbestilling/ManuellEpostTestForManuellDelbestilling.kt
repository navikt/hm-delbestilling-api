package no.nav.hjelpemidler.delbestilling.delbestilling

import no.nav.hjelpemidler.delbestilling.common.DelUkjent
import no.nav.hjelpemidler.delbestilling.common.DellinjeUkjentDel
import no.nav.hjelpemidler.delbestilling.common.Levering
import no.nav.hjelpemidler.delbestilling.testdata.delLinje
import no.nav.hjelpemidler.delbestilling.testdata.delbestilling
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue

class ManuellEpostTestForManuellDelbestilling {

    /**
     * Lager en lokal forhåndsvisning av epost for delbestilling for manuell behandling i prosjektrot,
     * så man kan enkelt se hvordan det vil bli seende ut uten å sende noe.
     */

    @Ignore
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
            navn = "Skiperlue for seilbåtentusiaster",
            ukjenteDeler = listOf(
                DellinjeUkjentDel(
                    delUkjent = DelUkjent(
                        hmsnr = null,
                        levArtNr = "456",
                        beskrivelse = "Skjerm til skipperlue",
                    ),
                    antall = 1,
                )
            ),
            levering = Levering.TIL_XK_LAGER,
            epostTekniker = "tekniker@nav.no",
        )

        val html = ManuellDelbestillingEpost(delbestilling, 19).tilHtml()
        val preview = Path.of("manual-email-preview.html").toAbsolutePath() // Setter path for preview

        Files.createDirectories(preview.parent)
        Files.writeString(preview, html)

        assertTrue(Files.exists(preview))
        println("Lokal e-postvisning: $preview")
    }
}