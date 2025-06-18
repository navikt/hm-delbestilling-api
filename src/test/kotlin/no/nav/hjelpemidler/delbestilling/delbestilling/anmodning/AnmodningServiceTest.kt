package no.nav.hjelpemidler.delbestilling.delbestilling.anmodning

import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import no.nav.hjelpemidler.delbestilling.common.Enhet
import no.nav.hjelpemidler.delbestilling.testdata.TestDatabase
import no.nav.hjelpemidler.delbestilling.testdata.delLinje
import no.nav.hjelpemidler.delbestilling.testdata.delbestilling
import no.nav.hjelpemidler.delbestilling.testdata.delbestillingSak
import no.nav.hjelpemidler.delbestilling.infrastructure.email.Email
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Oebs
import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction.Transaction
import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction.TransactionScopeFactory
import no.nav.hjelpemidler.delbestilling.infrastructure.slack.Slack
import no.nav.hjelpemidler.delbestilling.infrastructure.norg.Norg
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.BeforeEach
import kotlin.test.assertEquals

class AnmodningServiceTest {

    private var ds = TestDatabase.testDataSource
    private val transaction = Transaction(ds, TransactionScopeFactory())
    val oebs = mockk<Oebs>()
    val norg = mockk<Norg>()
    val slack = mockk<Slack>(relaxed = true)
    val email = mockk<Email>(relaxed = true)
    val anmodningService = AnmodningService(transaction, oebs, norg, slack, email, mockk())

    @BeforeEach
    fun setup() {
        TestDatabase.cleanAndMigrate(ds)
    }

    @Test
    fun `skal filtrer ut riktige deler til anmodning`() = runTest {
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
    fun `test generering av epostmelding`() = runTest {
        val melding = anmodningService.sendAnmodningRapport(
            Anmodningrapport(
                enhet = Enhet.OSLO,
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