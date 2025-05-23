package no.nav.hjelpemidler.delbestilling.testdata

import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingRepository
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.AnmodningRepository
import no.nav.hjelpemidler.delbestilling.fakes.FakeTransaction
import no.nav.hjelpemidler.delbestilling.oppslag.PiloterService
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.Grunndata
import no.nav.hjelpemidler.delbestilling.fakes.GrunndataClientFake
import no.nav.hjelpemidler.delbestilling.infrastructure.metrics.Metrics
import no.nav.hjelpemidler.delbestilling.fakes.NorgClientFake
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Oebs
import no.nav.hjelpemidler.delbestilling.fakes.OebsApiProxyFake
import no.nav.hjelpemidler.delbestilling.fakes.OebsSinkFake
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.Pdl
import no.nav.hjelpemidler.delbestilling.fakes.PdlClientFake
import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction.TransactionScope
import no.nav.hjelpemidler.delbestilling.infrastructure.slack.Slack
import no.nav.hjelpemidler.delbestilling.oppslag.BerikMedDagerSidenForrigeBatteribestilling
import no.nav.hjelpemidler.delbestilling.oppslag.BerikMedLagerstatus
import no.nav.hjelpemidler.delbestilling.oppslag.FinnDelerTilHjelpemiddel
import no.nav.hjelpemidler.delbestilling.oppslag.OppslagService
import no.nav.hjelpemidler.hjelpemidlerdigitalSoknadapi.tjenester.norg.Norg

class TestContext {
    // Mocks
    val metrics = mockk<Metrics>(relaxed = true)
    val slack = mockk<Slack>(relaxed = true)
    val delbestillingRepository = mockk<DelbestillingRepository>() // TODO erstatt med fake
    val anmodningRepository = mockk<AnmodningRepository>() // TODO erstatt med fake
    val transactionScope = TransactionScope(anmodningRepository, delbestillingRepository)

    val transactional = FakeTransaction(transactionScope)

    // Grunndata
    val grunndataClient = GrunndataClientFake()
    val grunndata = Grunndata(grunndataClient)

    // Norg
    val norgClient = NorgClientFake()
    val norg = Norg(norgClient)

    // OeBS
    val lager = FakeOebsLager()
    val oebsSink = OebsSinkFake(lager)
    val oebsApiProxy = OebsApiProxyFake(lager)
    val oebs = Oebs(oebsApiProxy, oebsSink)

    // PDL
    val pdlClient = PdlClientFake()
    val pdl = Pdl(pdlClient)

    // Oppslag
    val piloterService = PiloterService(norg)
    val finnDelerTilHjelpemiddel = FinnDelerTilHjelpemiddel(grunndata, slack, metrics)
    val berikMedLagerstatus = BerikMedLagerstatus(oebs, metrics)
    val berikMedDagerSidenForrigeBatteribestilling = BerikMedDagerSidenForrigeBatteribestilling(transactional)
    val oppslagService = OppslagService(
        pdl,
        oebs,
        piloterService,
        finnDelerTilHjelpemiddel,
        berikMedLagerstatus,
        berikMedDagerSidenForrigeBatteribestilling
    )
}

fun runWithTestContext(block: suspend TestContext.() -> Unit) {
    runTest {
        with(TestContext()) {
            block()
        }
    }
}