package no.nav.hjelpemidler.delbestilling.oppslag

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import no.nav.hjelpemidler.delbestilling.delbestilling.PiloterService
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.Grunndata
import no.nav.hjelpemidler.delbestilling.infrastructure.metrics.Metrics
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Oebs
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.OebsPersoninfo
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.Pdl
import no.nav.hjelpemidler.delbestilling.infrastructure.slack.Slack
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class OppslagServiceTest {

    val brukersFnr = "26928698180"
    val teknikerNavn = "Turid Tekniker"
    val brukersKommunenr = "1234"

    private val slack = mockk<Slack>(relaxed = true)
    private val metrics = mockk<Metrics>(relaxed = true)

    private val pdl = mockk<Pdl>().apply {
        coEvery { hentKommunenummer(any()) } returns brukersKommunenr
        coEvery { hentFornavn(any()) } returns teknikerNavn
    }
    private val oebs = mockk<Oebs>(relaxed = true).apply {
        coEvery { hentPersoninfo(any()) } returns listOf(OebsPersoninfo(brukersKommunenr))
        coEvery { hentFnrLeietaker(any(), any()) } returns brukersFnr
    }
    private val grunndata = mockk<Grunndata>()

    private val piloterService = mockk<PiloterService>(relaxed = true)
    private val finnDelerTilHjelpemiddel = FinnDelerTilHjelpemiddel(grunndata, slack, metrics)
    private val berikMedLagerstatus = BerikMedLagerstatus(oebs, metrics)

    private val oppslagService = OppslagService(
        pdl,
        oebs,
        piloterService,
        finnDelerTilHjelpemiddel,
        berikMedLagerstatus
    )

    @Test
    fun `skal feile dersom oppslag inneholder deler uten lagerstatus`() = runTest {
        val azaleaHmsnr = "097765"
        val azaleaSerienr = "123456"
        coEvery { oebs.hentLagerstatusForKommunenummer(any(), any()) } returns emptyList()
        coEvery { grunndata.hentProdukt(azaleaHmsnr) } returns null

        assertThrows<IllegalStateException> {
            oppslagService.slåOppHjelpemiddel(azaleaHmsnr, azaleaSerienr)
        }
    }
}