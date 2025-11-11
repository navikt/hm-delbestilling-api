package no.nav.hjelpemidler.delbestilling.testdata

import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingService
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.AnmodningService
import no.nav.hjelpemidler.delbestilling.fakes.ElectorFake
import no.nav.hjelpemidler.delbestilling.fakes.GraphClientFake
import no.nav.hjelpemidler.delbestilling.fakes.GrunndataClientFake
import no.nav.hjelpemidler.delbestilling.fakes.LocalHostFake
import no.nav.hjelpemidler.delbestilling.fakes.NorgClientFake
import no.nav.hjelpemidler.delbestilling.fakes.OebsApiProxyFake
import no.nav.hjelpemidler.delbestilling.fakes.OebsSinkFake
import no.nav.hjelpemidler.delbestilling.fakes.OppslagClientFake
import no.nav.hjelpemidler.delbestilling.fakes.PdlClientFake
import no.nav.hjelpemidler.delbestilling.infrastructure.email.Email
import no.nav.hjelpemidler.delbestilling.infrastructure.geografi.Kommuneoppslag
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.Grunndata
import no.nav.hjelpemidler.delbestilling.infrastructure.leaderElection.ErLeder
import no.nav.hjelpemidler.delbestilling.infrastructure.metrics.Metrics
import no.nav.hjelpemidler.delbestilling.infrastructure.norg.Norg
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.FinnLagerenhet
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Oebs
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.Pdl
import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction.Transaction
import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction.TransactionScopeFactory
import no.nav.hjelpemidler.delbestilling.infrastructure.slack.Slack
import no.nav.hjelpemidler.delbestilling.oppslag.BerikMedDagerSidenForrigeBatteribestilling
import no.nav.hjelpemidler.delbestilling.oppslag.BerikMedLagerstatus
import no.nav.hjelpemidler.delbestilling.oppslag.FinnDelerTilHjelpemiddel
import no.nav.hjelpemidler.delbestilling.oppslag.OppslagService
import no.nav.hjelpemidler.delbestilling.oppslag.PiloterService
import no.nav.hjelpemidler.delbestilling.ordrestatus.DelbestillingStatusService
import no.nav.hjelpemidler.delbestilling.rapportering.Rapportering
import java.time.Clock


class TestContext(
    clock: Clock,
) {
    // Mocks
    val metrics = mockk<Metrics>(relaxed = true)
    val slack = mockk<Slack>(relaxed = true)

    // Database
    val transaction by lazy {
        Transaction(TestDatabase.cleanAndMigratedDataSource(), TransactionScopeFactory())
    }

    // Email
    val emailClient = GraphClientFake()
    val email = Email(emailClient)

    // Leader election
    val elector = ElectorFake()
    val localHost = LocalHostFake()
    val erLeder = ErLeder(elector, localHost)

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
    val finnLagerenhet = FinnLagerenhet(norg, slack)
    val oebs = Oebs(oebsApiProxy, oebsSink, finnLagerenhet)

    // PDL
    val pdlClient = PdlClientFake()
    val pdl = Pdl(pdlClient)

    // Oppslag
    val piloterService = PiloterService(oebs)
    val finnDelerTilHjelpemiddel = FinnDelerTilHjelpemiddel(grunndata, slack, metrics)
    val berikMedLagerstatus = BerikMedLagerstatus(oebs, metrics)
    val berikMedDagerSidenForrigeBatteribestilling by lazy { BerikMedDagerSidenForrigeBatteribestilling(transaction) }
    val oppslagService by lazy {
        OppslagService(
            pdl,
            oebs,
            piloterService,
            finnDelerTilHjelpemiddel,
            berikMedLagerstatus,
            berikMedDagerSidenForrigeBatteribestilling,
        )
    }

    // Delbestilling
    val oppslagClient = OppslagClientFake()
    val kommuneoppslag = Kommuneoppslag(oppslagClient)
    val anmodningService = AnmodningService(transaction, oebs, slack, email, grunndata)
    val delbestillingService =
        DelbestillingService(transaction, pdl, oebs, kommuneoppslag, metrics, slack, anmodningService)

    // Status
    val delbestillingStatusService = DelbestillingStatusService(transaction, oebs, metrics, slack)

    // Rapportering
    val rapportering = Rapportering(delbestillingService, erLeder, clock)
}

fun runWithTestContext(clock: Clock = Clock.systemDefaultZone(), block: suspend TestContext.() -> Unit) {
    runTest {
        with(TestContext(clock)) {
            block()
        }
    }
}