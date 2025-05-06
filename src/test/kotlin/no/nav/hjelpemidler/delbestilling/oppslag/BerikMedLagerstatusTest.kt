package no.nav.hjelpemidler.delbestilling.oppslag

import no.nav.hjelpemidler.delbestilling.delbestilling.model.Hmsnr
import no.nav.hjelpemidler.delbestilling.testdata.Testdata
import no.nav.hjelpemidler.delbestilling.testdata.runWithTestContext
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BerikMedLagerstatusTest {

    @Test
    fun `skal berike alle deler med lagerstatus`() = runWithTestContext {
        val deler = listOf(del("111111"), del("222222"), del("333333"))
        deler.forEach { lager.set(it.hmsnr, antall = 3) }

        val beriket = berikMedLagerstatus.execute(hjelpemiddel(deler), Testdata.defaultKommunenummer)

        assertEquals(3, beriket.deler.size)
        assertTrue(beriket.deler.all { it.lagerstatus != null })
    }

    @Test
    fun `skal berike lagerstatus med riktige antall og minmax-verdi`() = runWithTestContext {
        val del = del("111111")
        lager.set(del.hmsnr, antall = 4, minmax = false)

        val beriket = berikMedLagerstatus.execute(hjelpemiddel(listOf(del)), Testdata.defaultKommunenummer)

        val lagerstatus = assertNotNull(beriket.deler.first().lagerstatus)
        assertEquals(4, lagerstatus.antallDelerPåLager)
        assertFalse(lagerstatus.minmax)
    }

    @Test
    fun `skal feile dersom lagerstatus mangler`() = runWithTestContext {
        val del = del("111111")
        lager.setNull(del.hmsnr)

        val exception = runCatching {
            berikMedLagerstatus.execute(hjelpemiddel(listOf(del)), Testdata.defaultKommunenummer)
        }.exceptionOrNull()

        assertTrue(exception is IllegalStateException)
        assertTrue(exception.message!!.contains(del.hmsnr))
    }
}

fun del(hmsnr: Hmsnr) = Del(
    hmsnr = hmsnr,
    navn = hmsnr,
    kategori = "Batteri",
    maksAntall = 2
)

fun hjelpemiddel(deler: List<Del>) = Hjelpemiddel(
    hmsnr = Testdata.defaultHjmHmsnr,
    navn = Testdata.defaultHjmNavn,
    deler = deler
)