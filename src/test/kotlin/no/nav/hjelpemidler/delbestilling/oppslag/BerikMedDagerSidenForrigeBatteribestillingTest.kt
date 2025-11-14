package no.nav.hjelpemidler.delbestilling.oppslag

import no.nav.hjelpemidler.delbestilling.fakes.GrunndataTestHmsnr
import no.nav.hjelpemidler.delbestilling.testdata.Testdata
import no.nav.hjelpemidler.delbestilling.testdata.delbestillingMedBatteri
import no.nav.hjelpemidler.delbestilling.testdata.fixtures.gittDelbestilling
import no.nav.hjelpemidler.delbestilling.runWithTestContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import kotlin.test.Test
import kotlin.test.assertTrue


class BerikMedDagerSidenForrigeBatteribestillingTest {

    @Test
    fun `skal returnere uendret hjelpemiddel dersom det ikke har batteri`() = runWithTestContext {
        val hjelpemiddel = hjelpemiddel(deler = listOf(del(kategori = "Svinghjul")))

        val beriketHjelpemiddel = berikMedDagerSidenForrigeBatteribestilling(hjelpemiddel, Testdata.defaultHjmSerienr)

        assertEquals(hjelpemiddel, beriketHjelpemiddel)
    }

    @Test
    fun `skal returnere hjelpemiddel med null dager dersom hjelpemiddel har batteri, men ingen tidligere batteribestilling`() =
        runWithTestContext {
            val hjelpemiddel = hjelpemiddel(deler = listOf(del(kategori = "Batteri")))

            val beriketHjelpemiddel =
                berikMedDagerSidenForrigeBatteribestilling(hjelpemiddel, Testdata.defaultHjmSerienr)

            assertTrue(beriketHjelpemiddel.harBatteri())
            assertNull(beriketHjelpemiddel.antallDagerSidenSistBatteribestilling)
        }

    @Test
    fun `skal returnere hjelpemiddel med korrekt antall dager siden forrige batteribestilling`() = runWithTestContext {
        gittDelbestilling(delbestillingMedBatteri(), dagerSidenOpprettelse = 10)
        gittDelbestilling(delbestillingMedBatteri(), dagerSidenOpprettelse = 25)

        val resultat = oppslagService.slåOppHjelpemiddel(GrunndataTestHmsnr.HAR_BATTERI, Testdata.defaultHjmSerienr)

        assertTrue(resultat.hjelpemiddel.harBatteri())
        assertEquals(10, resultat.hjelpemiddel.antallDagerSidenSistBatteribestilling)
    }
}


