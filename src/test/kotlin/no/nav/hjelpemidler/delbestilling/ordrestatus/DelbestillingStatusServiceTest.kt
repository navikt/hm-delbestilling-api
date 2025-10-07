package no.nav.hjelpemidler.delbestilling.ordrestatus

import no.nav.hjelpemidler.delbestilling.common.DellinjeStatus
import no.nav.hjelpemidler.delbestilling.common.Status
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.DelUtenDekningStatus
import no.nav.hjelpemidler.delbestilling.testdata.Testdata
import no.nav.hjelpemidler.delbestilling.testdata.fixtures.annulerSak
import no.nav.hjelpemidler.delbestilling.testdata.fixtures.hentDelUtenDekning
import no.nav.hjelpemidler.delbestilling.testdata.fixtures.hentDelbestilling
import no.nav.hjelpemidler.delbestilling.testdata.fixtures.hentRader
import no.nav.hjelpemidler.delbestilling.testdata.fixtures.oppdaterDellinjeStatus
import no.nav.hjelpemidler.delbestilling.testdata.fixtures.opprettDelbestilling
import no.nav.hjelpemidler.delbestilling.testdata.fixtures.opprettDelbestillingMedDel
import no.nav.hjelpemidler.delbestilling.testdata.fixtures.opprettDelbestillingMedDeler
import no.nav.hjelpemidler.delbestilling.testdata.runWithTestContext
import no.nav.hjelpemidler.time.TIME_ZONE_EUROPE_OSLO
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.util.TimeZone
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue


class DelbestillingStatusServiceTest {

    @Test
    fun `delbestilling skal få status INNSENDT ved innsending`() = runWithTestContext {
        val saksnummer = opprettDelbestilling().saksnummer!!

        with(hentDelbestilling(saksnummer)) {
            assertEquals(Status.INNSENDT, status)
            assertEquals(null, oebsOrdrenummer)
        }
    }

    @Test
    fun `skal oppdatere status på delbestilling`() = runWithTestContext {
        val saksnummer = opprettDelbestilling().saksnummer!!
        val oebsOrdrenummer = "123"
        delbestillingStatusService.oppdaterStatus(saksnummer, Status.KLARGJORT, oebsOrdrenummer)

        with(hentDelbestilling(saksnummer)) {
            assertEquals(Status.KLARGJORT, this.status)
            assertEquals(oebsOrdrenummer, this.oebsOrdrenummer)
        }
    }

    @Test
    fun `skal ikke kunne oppdatere delbestilling til en tidligere status`() = runWithTestContext {
        val saksnummer = opprettDelbestilling().saksnummer!!
        val oebsOrdrenummer = "123"

        delbestillingStatusService.oppdaterStatus(saksnummer, Status.KLARGJORT, oebsOrdrenummer)

        // Denne skal ikke ha noen effekt
        delbestillingStatusService.oppdaterStatus(saksnummer, Status.REGISTRERT, oebsOrdrenummer)

        with(hentDelbestilling(saksnummer)) {
            assertEquals(Status.KLARGJORT, status)
        }
    }

    @Test
    fun `statusoppdatering skal feile når det er mismatch i oebsOrdrenummer`() = runWithTestContext {
        val saksnummer = opprettDelbestilling().saksnummer!!
        delbestillingStatusService.oppdaterStatus(saksnummer, Status.REGISTRERT, "111")

        assertThrows<IllegalArgumentException> {
            delbestillingStatusService.oppdaterStatus(saksnummer, Status.REGISTRERT, "222")
        }
    }

    @Test
    fun `skal oppdatere status på dellinjer`() = runWithTestContext {
        // Appen vil alltid kjøre med Europe/Oslo (ref. Dockerimage), men setter det eksplisitt her pga Github Runners bruker UTC
        TimeZone.setDefault(TIME_ZONE_EUROPE_OSLO)

        val oebsOrdrenummer = "8725414"
        val hmsnrDel1 = "111111"
        val hmsnrDel2 = "222222"

        val saksnummer = opprettDelbestillingMedDeler(hmsnrDel1, hmsnrDel2).saksnummer!!

        delbestillingStatusService.oppdaterStatus(saksnummer, Status.KLARGJORT, oebsOrdrenummer)

        // Skipningsbekreft første del
        val datoSkipningsbekreftet = LocalDate.of(2023, 9, 29)
        delbestillingStatusService.oppdaterDellinjeStatus(
            oebsOrdrenummer,
            DellinjeStatus.SKIPNINGSBEKREFTET,
            hmsnrDel1,
            datoSkipningsbekreftet
        )

        with(hentDelbestilling(saksnummer)) {
            assertEquals(Status.DELVIS_SKIPNINGSBEKREFTET, status)

            val del1 = delbestilling.deler.first { it.del.hmsnr == hmsnrDel1 }
            assertEquals(DellinjeStatus.SKIPNINGSBEKREFTET, del1.status)
            assertEquals(datoSkipningsbekreftet, del1.datoSkipningsbekreftet)
            assertEquals(LocalDate.of(2023, 10, 2), del1.forventetLeveringsdato)

            val del2 = delbestilling.deler.first { it.del.hmsnr == hmsnrDel2 }
            assertNull(del2.status)
            assertNull(del2.datoSkipningsbekreftet)
            assertNull(del2.forventetLeveringsdato)
        }
    }

    @Test
    fun `skal ignorere skipningsbekreftelse for ukjent ordrenr`() = runWithTestContext {
        val oebsOrdrenummer = "8725414"
        assertNull(hentDelbestilling(oebsOrdrenummer))

        assertDoesNotThrow { oppdaterDellinjeStatus(oebsOrdrenummer) }
    }

    @Test
    fun `skal annulere deler_uten_dekning for relevant sak`() = runWithTestContext {
        lager.tømAlleDeler()
        val hmsnr = Testdata.defaultDelHmsnr

        opprettDelbestillingMedDel(hmsnr, antall = 2)
        val saksnummerTilAnnulering = opprettDelbestillingMedDel(hmsnr, antall = 3).saksnummer!!

        assertEquals(5, hentDelUtenDekning(hmsnr).antall, "Alle deler (2+3) skal ligge til anmodning")

        annulerSak(saksnummerTilAnnulering)

        assertEquals(2, hentDelUtenDekning(hmsnr).antall, "Kun delen som ikke er annulert skal ligge til anmodning")
    }

    @Test
    fun `skal ikke annulere sak som er ferdig anmodet`() = runWithTestContext {
        lager.tømAlleDeler()
        val hmsnr = Testdata.defaultDelHmsnr

        val saksnummer = opprettDelbestillingMedDel(hmsnr, antall = 3).saksnummer!!
        delbestillingService.rapporterDelerTilAnmodning()
        annulerSak(saksnummer)

        val rader = transaction { delUtenDekningDao.hentRader() }
        assertEquals(1, rader.size)
        with (rader.first()) {
            assertEquals(DelUtenDekningStatus.BEHANDLET, status)
            assertNotNull(behandletTidspunkt)
        }
    }
}