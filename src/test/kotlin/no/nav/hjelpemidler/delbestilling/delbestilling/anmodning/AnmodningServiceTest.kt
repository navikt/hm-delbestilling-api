package no.nav.hjelpemidler.delbestilling.delbestilling.anmodning

import kotlinx.coroutines.test.runTest
import no.nav.hjelpemidler.delbestilling.common.Lager
import no.nav.hjelpemidler.delbestilling.testdata.delLinje
import no.nav.hjelpemidler.delbestilling.testdata.delbestilling
import no.nav.hjelpemidler.delbestilling.testdata.delbestillingSak
import no.nav.hjelpemidler.delbestilling.testdata.runWithTestContext
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AnmodningServiceTest {

    @Test
    fun `skal filtrer ut riktige deler til anmodning`() = runWithTestContext {
        val hmsnrMinmax = "333333"
        val hmsnrPåLager = "444444"
        val hmsnrFåPåLager = "555555"

        val sak = delbestillingSak(
            delbestilling(
                deler = listOf(
                    delLinje(
                        antall = 3,
                        hmsnr = hmsnrMinmax,
                        lagerstatus = lagerstatus(hmsnr = hmsnrMinmax, antall = 1, minmax = true)
                    ),
                    delLinje(
                        antall = 4,
                        hmsnr = hmsnrPåLager,
                        lagerstatus = lagerstatus(hmsnr = hmsnrPåLager, antall = 20)
                    ),
                    delLinje(
                        antall = 5,
                        hmsnr = hmsnrFåPåLager,
                        lagerstatus = lagerstatus(hmsnr = hmsnrFåPåLager, antall = 1)
                    ),
                )
            )
        )
        val delerUtenDekning = anmodningService.finnDelerUtenDekning(sak)

        assertEquals(1, delerUtenDekning.size)
        assertEquals(hmsnrFåPåLager, delerUtenDekning.first().hmsnr)
        assertEquals(4, delerUtenDekning.first().antallSomMåAnmodes)
    }

    @Test
    fun `test genererAnmodningsrapporter`() = runTest {
        // anmodningService.genererAnmodningsrapporter()
    }

    @Test
    fun `test generering av epostmelding`() = runWithTestContext {
        val melding = anmodningService.sendAnmodningRapport(
            Anmodningrapport(
                lager = Lager.OSLO,
                anmodningsbehov = listOf(
                    AnmodningsbehovForDel(
                        hmsnr = "123456",
                        navn = "Batteri 80A inkl poler",
                        antallBestilt = 10,
                        antallPåLager = 0,
                        erPåMinmax = false,
                        antallSomMåAnmodes = 5,
                        leverandørnavn = "Etac AS"
                    ),
                    AnmodningsbehovForDel(
                        hmsnr = "478294",
                        navn = "Dekk Schwalbe Marathon Plus punkteringsbeskyttet 24",
                        antallBestilt = 10,
                        antallPåLager = 0,
                        erPåMinmax = false,
                        antallSomMåAnmodes = 15,
                        leverandørnavn = "Etac AS"
                    ),
                    AnmodningsbehovForDel(
                        hmsnr = "738137",
                        navn = "Hjul bak",
                        antallBestilt = 10,
                        antallPåLager = 0,
                        erPåMinmax = false,
                        antallSomMåAnmodes = 7,
                        leverandørnavn = "Invacare"
                    ),
                    AnmodningsbehovForDel(
                        hmsnr = "738923",
                        navn = "Hjul foran",
                        antallBestilt = 10,
                        antallPåLager = 0,
                        erPåMinmax = false,
                        antallSomMåAnmodes = 107,
                        leverandørnavn = "Invacare"
                    )
                ),
                delerSomIkkeLengerMåAnmodes = emptyList()
            )
        )

        assertEquals(
            """
            Hei!

            Disse delene er bestilt digitalt, men er ikke på lager. Dere må derfor sende anmodning på følgende:

            Leverandør: Etac AS
            123456 (Batteri 80A inkl poler): Må anmodes 5 stk.
            478294 (Dekk Schwalbe Marathon Plus punkteringsbeskyttet 24): Må anmodes 15 stk.

            Leverandør: Invacare
            738137 (Hjul bak): Må anmodes 7 stk.
            738923 (Hjul foran): Må anmodes 107 stk.

            Dersom dere har spørsmål til dette så kan dere svare oss tilbake på denne e-posten.

            Vennlig hilsen
            DigiHoT
        """.trimIndent(), melding
        )
    }
}