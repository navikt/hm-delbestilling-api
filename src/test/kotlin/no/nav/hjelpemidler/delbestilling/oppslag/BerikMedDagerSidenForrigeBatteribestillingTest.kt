package no.nav.hjelpemidler.delbestilling.oppslag

import io.mockk.every
import no.nav.hjelpemidler.delbestilling.testdata.Testdata
import no.nav.hjelpemidler.delbestilling.testdata.delLinje
import no.nav.hjelpemidler.delbestilling.testdata.delbestilling
import no.nav.hjelpemidler.delbestilling.testdata.delbestillingSak
import no.nav.hjelpemidler.delbestilling.testdata.runWithTestContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import java.time.LocalDateTime
import kotlin.test.Test


class BerikMedDagerSidenForrigeBatteribestillingTest {

    @Test
    fun `skal returnere uendret hjelpemiddel dersom det ikke har batteri`() = runWithTestContext {
        val hjelpemiddel = hjelpemiddel(deler = listOf(del(kategori = "Svinghjul")))

        val resultat = berikMedDagerSidenForrigeBatteribestilling.berik(hjelpemiddel, Testdata.defaultHjmSerienr)

        assertEquals(hjelpemiddel, resultat)
    }

    @Test
    fun `skal returnere hjelpemiddel med null dager dersom hjelpemiddel har batteri, men ingen tidligere batteribestilling`() =
        runWithTestContext {
            val hjelpemiddel = hjelpemiddel(deler = listOf(del(kategori = "Batteri")))
            every { delbestillingRepository.hentDelbestillinger(any(), any()) } returns emptyList()

            val resultat = berikMedDagerSidenForrigeBatteribestilling.berik(hjelpemiddel, Testdata.defaultHjmSerienr)

            assertNull(resultat.antallDagerSidenSistBatteribestilling)
        }

    @Test
    fun `skal returnere hjelpemiddel med korrekt antall dager siden forrige batteribestilling`() = runWithTestContext {
        val hjelpemiddel = hjelpemiddel(deler = listOf(del(kategori = "Batteri")))
        val opprettetDato = LocalDateTime.now().minusDays(10)

        val delbestillingSak = delbestillingSak(
            delbestilling = delbestilling(deler = listOf(delLinje(kategori = "Batteri"))),
            opprettet = opprettetDato,
        )
        every { delbestillingRepository.hentDelbestillinger(any(), any()) } returns listOf(delbestillingSak)

        val resultat = berikMedDagerSidenForrigeBatteribestilling.berik(hjelpemiddel, Testdata.defaultHjmSerienr)

        assertEquals(10, resultat.antallDagerSidenSistBatteribestilling)
    }
}


