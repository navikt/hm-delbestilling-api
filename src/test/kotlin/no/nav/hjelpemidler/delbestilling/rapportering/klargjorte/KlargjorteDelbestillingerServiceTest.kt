package no.nav.hjelpemidler.delbestilling.rapportering.klargjorte

import no.nav.hjelpemidler.delbestilling.common.Del
import no.nav.hjelpemidler.delbestilling.common.DelLinje
import no.nav.hjelpemidler.delbestilling.common.Delbestilling
import no.nav.hjelpemidler.delbestilling.common.DelbestillingSak
import no.nav.hjelpemidler.delbestilling.common.Kilde
import no.nav.hjelpemidler.delbestilling.common.Lager
import no.nav.hjelpemidler.delbestilling.common.Levering
import no.nav.hjelpemidler.delbestilling.common.Status
import no.nav.hjelpemidler.delbestilling.runWithTestContext
import no.nav.hjelpemidler.delbestilling.testdata.fixtures.gittDelbestilling
import no.nav.hjelpemidler.text.toUUID
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals

class KlargjorteDelbestillingerServiceTest {
    @Test
    fun `skal generere rapport for delbestillinger som har status KLARGJORT`() = runWithTestContext {
        // 2 klargjorte delbestillinger for lager Oslo
        gittDelbestilling(lagerEnhet = Lager.OSLO, status = Status.INNSENDT)
        gittDelbestilling(lagerEnhet = Lager.OSLO, status = Status.KLARGJORT)
        gittDelbestilling(lagerEnhet = Lager.OSLO, status = Status.KLARGJORT)

        // 1 klargjort delbestilling for lager Finnmark
        gittDelbestilling(lagerEnhet = Lager.FINNMARK, status = Status.KLARGJORT)

        // 1 skipet delbestilling for lager Vestland-Bergen
        gittDelbestilling(lagerEnhet = Lager.VESTLAND_BERGEN, status = Status.SKIPNINGSBEKREFTET)

        val rapporter = klargjorteDelbestillingerService.rapporterKlargjorteDelbestillinger(0)
        assertEquals(2, rapporter.size)
        assertEquals(2, rapporter[0].delbestillinger.size)
        assertEquals(1, rapporter[1].delbestillinger.size)

        assertEquals("Oslo", rapporter[0].lager.navn)
        assertEquals("Finnmark", rapporter[1].lager.navn)
    }

    @Test
    fun `skal ikke generere rapporter for delbestillinger med status KLARGJORT som ble opprettet etter grense på antall dager`() = runWithTestContext {
        gittDelbestilling(status = Status.KLARGJORT)

        val rapporter = klargjorteDelbestillingerService.rapporterKlargjorteDelbestillinger(eldreEnnDager = 1)
        assertEquals(0, rapporter.size)
    }

    @Test
    fun `skal sende e-post for delbestillinger med status KLARGJORT med riktig innhold()`() = runWithTestContext {
        val rapport = KlargjorteDelbestillingerRapport(
            lager = Lager.OSLO,
            delbestillinger = listOf(
                DelbestillingSak(
                    saksnummer = 2,
                    delbestilling = Delbestilling(
                        id = "e682c2c1-a7fc-40cb-95c5-17b6200bf6c7".toUUID(),
                        hmsnr = "236958",
                        serienr = "123456",
                        deler = listOf(
                            DelLinje(
                                del = Del(
                                    hmsnr = "150817",
                                    navn = """Dekk 24" mrs Schwalbe""",
                                    levArtNr = "1000038",
                                    kategori = "dekk",
                                    defaultAntall = 2,
                                    maksAntall = 2,
                                    imgs = emptyList(),
                                    lagerstatus = null,
                                    kilde = Kilde.MANUELL_LISTE
                                ),
                                antall = 1,
                                status = null,
                                datoSkipningsbekreftet = null,
                                forventetLeveringsdato = null,
                                lagerstatusPåBestillingstidspunkt = null
                            ),
                        ),
                        levering = Levering.TIL_XK_LAGER,
                        harOpplæringPåBatteri = false,
                        navn = "Panthera U3 Light",
                        status = Status.KLARGJORT,
                    ),
                    opprettet = LocalDateTime.parse("2025-11-21T12:43:50.483379"),
                    status = Status.KLARGJORT,
                    sistOppdatert = LocalDateTime.parse("2025-11-21T12:43:50.483379"),
                    oebsOrdrenummer = "1338",
                    brukersKommunenummer = "0301",
                    brukersKommunenavn = "Oslo",
                    enhetnr = "4703",
                    enhetnavn = "Oslo"
                ),

                DelbestillingSak(
                    saksnummer = 3,
                    delbestilling = Delbestilling(
                        id = "449be5ef-5773-4463-b0da-bafb86455eb1".toUUID(),
                        hmsnr = "236958",
                        serienr = "123456",
                        deler = listOf(
                            DelLinje(
                                del = Del(
                                    hmsnr = "278247",
                                    navn = """Slange 26" mrs""",
                                    levArtNr = "1000038",
                                    kategori = "slange",
                                    defaultAntall = 1,
                                    maksAntall = 2,
                                    imgs = emptyList(),
                                    lagerstatus = null,
                                    kilde = Kilde.MANUELL_LISTE
                                ),
                                antall = 1,
                                status = null,
                                datoSkipningsbekreftet = null,
                                forventetLeveringsdato = null,
                                lagerstatusPåBestillingstidspunkt = null
                            )
                        ),
                        levering = Levering.TIL_XK_LAGER,
                        harOpplæringPåBatteri = false,
                        navn = "Panthera U3 Light",
                        status = Status.KLARGJORT,
                    ),
                    opprettet = LocalDateTime.parse("2025-11-21T12:43:50.490317"),
                    status = Status.KLARGJORT,
                    sistOppdatert = LocalDateTime.parse("2025-11-21T12:43:50.490317"),
                    oebsOrdrenummer = "1337",
                    brukersKommunenummer = "0301",
                    brukersKommunenavn = "Oslo",
                    enhetnr = "4703",
                    enhetnavn = "Oslo"
                )
            )
        )

        val melding = klargjorteDelbestillingerService.sendKlargjorteDelbestillingerRapport(rapport)

        assertEquals("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Ikke-plukkede delbestillinger</title>
                <style>
                    table {
                        width: 100%;
                        border-collapse: collapse;
                    }
                    th, td {
                        padding: 10px;
                        border: 1px solid #ccc;
                        text-align: left;
                    }
                </style>
            </head>
            <body>
                <p>
                    Her er en oversikt over deler som ikke er plukket og sendt til kommunen fra lager Oslo. Dere vurderer om det trengs å gjøres noe spesielt med disse.
                    </br>
                    </br>
                    HMS lager: Oslo </br>
                </p>
                <table>
                    <thead>
                        <tr>
                            <th>Opprettet</th>
                            <th>OeBS-ordrenummer</th>
                            <th>Deler</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>2025-11-21</td>
                            <td>1337</td>
                            <td>278247 Slange 26" mrs (1stk)</td>
                        </tr>
                        <tr>
                            <td>2025-11-21</td>
                            <td>1338</td>
                            <td>150817 Dekk 24" mrs Schwalbe (1stk)</td>
                        </tr>
                    </tbody>
                </table>
            
                <p>Dersom dere har spørsmål til dette så kan dere svare oss tilbake på denne e-posten.</p>
                <p>
                    Vennlig hilsen
                    <br/> DigiHoT
                </p>
            </body>
            </html>
        """.fjernAllWhitespace(), melding.fjernAllWhitespace())
    }
}

private fun String.fjernAllWhitespace(): String {
    return this.replace("\\s".toRegex(), "")
}