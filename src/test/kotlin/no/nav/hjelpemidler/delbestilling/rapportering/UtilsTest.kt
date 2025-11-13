package no.nav.hjelpemidler.delbestilling.rapportering

import no.nav.hjelpemidler.delbestilling.testdata.MutableClock
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals


class UtilsTest {

    @Test
    fun `skal hoppe over helg ved beregning av neste ukedag`() {
        val fredag = LocalDateTime.of(2025, 11, 14, 10, 0)
        val mandag = fredag.plusDays(3)
        val clock = MutableClock(fredag)
        val nesteUkedag = kl01NesteUkedag(clock)

        assertEquals(mandag.dayOfYear, nesteUkedag.dayOfYear)
        assertEquals(1, nesteUkedag.hour)
    }

    @Test
    fun `skal bruke samme dag dersom klokken ikke er 0100 enda`() {
        val kl0020 = LocalDateTime.now().withHour(0).withMinute(20)
        val clock = MutableClock(kl0020)
        val resultat = kl01NesteUkedag(clock)

        assertEquals(kl0020.dayOfYear, resultat.dayOfYear)
        assertEquals(1, resultat.hour)
    }

}