package no.nav.hjelpemidler.delbestilling

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import no.nav.hjelpemidler.delbestilling.config.DatabaseConfig
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingRepository
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingService
import no.nav.hjelpemidler.delbestilling.oppslag.PiloterService
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.AnmodningRepository
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.AnmodningService
import no.nav.hjelpemidler.delbestilling.oppslag.Hjelpemiddeloversikt
import no.nav.hjelpemidler.delbestilling.infrastructure.email.Email
import no.nav.hjelpemidler.delbestilling.infrastructure.email.GraphClient
import no.nav.hjelpemidler.delbestilling.infrastructure.geografi.Kommuneoppslag
import no.nav.hjelpemidler.delbestilling.infrastructure.geografi.OppslagClient
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.Grunndata
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.GrunndataClient
import no.nav.hjelpemidler.delbestilling.infrastructure.kafka.Kafka
import no.nav.hjelpemidler.delbestilling.infrastructure.metrics.Metrics
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Oebs
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.OebsApiProxyClient
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.OebsSinkClient
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.Pdl
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.PdlClient
import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction.Transaction
import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction.TransactionScopeFactory
import no.nav.hjelpemidler.delbestilling.infrastructure.roller.Roller
import no.nav.hjelpemidler.delbestilling.infrastructure.roller.RollerClient
import no.nav.hjelpemidler.delbestilling.infrastructure.slack.Slack
import no.nav.hjelpemidler.delbestilling.oppslag.BerikMedDagerSidenForrigeBatteribestilling
import no.nav.hjelpemidler.delbestilling.oppslag.BerikMedLagerstatus
import no.nav.hjelpemidler.delbestilling.oppslag.FinnDelerTilHjelpemiddel
import no.nav.hjelpemidler.delbestilling.oppslag.OppslagService
import no.nav.hjelpemidler.delbestilling.ordrestatus.DelbestillingStatusService
import no.nav.hjelpemidler.hjelpemidlerdigitalSoknadapi.tjenester.norg.Norg
import no.nav.hjelpemidler.hjelpemidlerdigitalSoknadapi.tjenester.norg.NorgClient
import no.nav.hjelpemidler.http.openid.entraIDClient
import no.nav.tms.token.support.tokendings.exchange.TokendingsServiceBuilder
import kotlin.time.Duration.Companion.seconds


class AppContext {

    // Coroutine
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    fun shutdown() = backgroundScope.cancel("Shutting down application")

    // Database
    private val ds = DatabaseConfig.migratedDataSource
    private val transactionScopeFactory = TransactionScopeFactory()
    private val transactional = Transaction(ds, transactionScopeFactory)

    // Infrastructure
    private val entraIDClient = entraIDClient {
        cache(leeway = 10.seconds) {
            maximumSize = 100
        }
    }
    private val grunndata = Grunndata(GrunndataClient())
    private val kafka = Kafka()
    private val kommuneoppslag = Kommuneoppslag(OppslagClient())
    private val metrics = Metrics(kafka)
    private val norg = Norg(NorgClient())
    private val oebs = Oebs(OebsApiProxyClient(entraIDClient), OebsSinkClient(kafka))
    private val pdl = Pdl(PdlClient(entraIDClient))
    private val rollerClient = RollerClient(TokendingsServiceBuilder.buildTokendingsService())
    val email = Email(GraphClient(entraIDClient))
    val slack = Slack(transactional, backgroundScope)

    // Eksponert for custom plugin
    val roller = Roller(rollerClient)

    // Services
    private val piloterService = PiloterService(norg)
    private val finnDelerTilHjelpemiddel = FinnDelerTilHjelpemiddel(grunndata, slack, metrics)
    private val berikMedLagerstatus = BerikMedLagerstatus(oebs, metrics)
    private val berikMedDagerSidenForrigeBatteribestilling =
        BerikMedDagerSidenForrigeBatteribestilling(transactional)

    val anmodningService = AnmodningService(transactional, oebs, norg, slack, email, grunndata)
    val hjelpemiddeloversikt = Hjelpemiddeloversikt(grunndata, backgroundScope)
    val delbestillingService =
        DelbestillingService(transactional, pdl, oebs, kommuneoppslag, metrics, slack, anmodningService)
    val oppslagService = OppslagService(
        pdl,
        oebs,
        piloterService,
        finnDelerTilHjelpemiddel,
        berikMedLagerstatus,
        berikMedDagerSidenForrigeBatteribestilling
    )
    val delbestillingStatusService = DelbestillingStatusService(transactional, oebs, metrics)
}
