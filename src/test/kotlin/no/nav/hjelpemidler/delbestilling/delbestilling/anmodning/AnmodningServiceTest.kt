package no.nav.hjelpemidler.delbestilling.delbestilling.anmodning

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import no.nav.hjelpemidler.delbestilling.TestDatabase
import no.nav.hjelpemidler.delbestilling.delLinje
import no.nav.hjelpemidler.delbestilling.delbestilling
import no.nav.hjelpemidler.delbestilling.delbestillingSak
import no.nav.hjelpemidler.delbestilling.infrastructure.email.Email
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Oebs
import no.nav.hjelpemidler.delbestilling.slack.SlackClient
import no.nav.hjelpemidler.hjelpemidlerdigitalSoknadapi.tjenester.norg.NorgService
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.BeforeEach
import kotlin.test.assertEquals

class AnmodningServiceTest {

    private val ds = TestDatabase.testDataSource
    val repository = AnmodningRepository(ds)
    val oebs = mockk<Oebs>()
    val norg = mockk<NorgService>()
    val slack = mockk<SlackClient>(relaxed = true)
    val email = mockk<Email>(relaxed = true)
    val anmodningService = AnmodningService(repository, oebs, norg, slack, email, mockk())

    @BeforeEach
    fun setup() {
        TestDatabase.cleanAndMigrate(ds)
    }

    @Test
    fun `skal filtrer ut riktige deler til anmodning`() = runTest {
        val hmsnrMinmax = "333333"
        val hmsnrPåLager = "444444"
        val hmsnrFåPåLager = "555555"
        coEvery { oebs.hentLagerstatusForKommunenummer(any(), any()) } returns listOf(
            lagerstatus(hmsnr = hmsnrMinmax, antall = 1, minmax = true),
            lagerstatus(hmsnr = hmsnrPåLager, antall = 20),
            lagerstatus(hmsnr = hmsnrFåPåLager, antall = 1)
        )

        val sak = delbestillingSak(
            delbestilling(
                deler = listOf(
                    delLinje(antall = 3, hmsnr = hmsnrMinmax),
                    delLinje(antall = 4, hmsnr = hmsnrPåLager),
                    delLinje(antall = 5, hmsnr = hmsnrFåPåLager),
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
    fun `test generering av epostmelding`() = runTest {
        val melding = anmodningService.sendAnmodningRapport(
            Anmodningrapport(
                enhetnr = "4703",
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
                )
            )
        )

        println(melding)
    }
}