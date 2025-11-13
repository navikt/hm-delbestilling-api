package no.nav.hjelpemidler.delbestilling.infrastructure.oebs

import no.nav.hjelpemidler.delbestilling.common.Lager
import no.nav.hjelpemidler.delbestilling.fakes.HmsEnhet.HMS_AKERSHUS
import no.nav.hjelpemidler.delbestilling.fakes.HmsEnhet.HMS_TROMS_OG_FINNMARK
import no.nav.hjelpemidler.delbestilling.runWithTestContext
import no.nav.hjelpemidler.delbestilling.testdata.Kommunenummer.KARASJOK
import no.nav.hjelpemidler.delbestilling.testdata.Kommunenummer.NÆRØYSUND
import no.nav.hjelpemidler.domain.geografi.Kommune.Companion.BÆRUM
import no.nav.hjelpemidler.domain.geografi.Kommune.Companion.TROMSØ
import no.nav.hjelpemidler.domain.geografi.Kommune.Companion.TRONDHEIM
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FinnLagerenhetTest {

    @Test
    fun `enhet 4702 Akershus skal mappes til 4703 Oslo`() = runWithTestContext {
        val lager = finnLagerenhet(BÆRUM.nummer)
        assertEquals(Lager.OSLO, lager)
    }

    @Test
    fun `kommuner i Troms skal mappes til 4719 Troms`() = runWithTestContext {
        val lager = finnLagerenhet(TROMSØ.nummer)
        assertEquals(Lager.TROMS, lager)
    }

    @Test
    fun `kommuner i Finnmark skal mappes til 4720 Finnmark`() = runWithTestContext {
        val lager = finnLagerenhet(KARASJOK)
        assertEquals(Lager.FINNMARK, lager)
    }

    @Test
    fun `kommuner som tidligere tilhørte Sør-Trøndelag skal mappes til 4716 Sør-Trøndelag`() = runWithTestContext {
        val lager = finnLagerenhet(TRONDHEIM.nummer)
        assertEquals(Lager.SØR_TRØNDELAG, lager)
    }

    @Test
    fun `kommuner som tidligere tilhørte Nord-trøndelag skal mappes til 4717 Nord-Trøndelag`() = runWithTestContext {
        val lager = finnLagerenhet(NÆRØYSUND)
        assertEquals(Lager.NORD_TRØNDELAG, lager)
    }
}