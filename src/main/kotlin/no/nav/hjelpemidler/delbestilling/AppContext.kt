package no.nav.hjelpemidler.delbestilling

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import no.nav.hjelpemidler.delbestilling.config.DatabaseConfig
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingRepository
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingService
import no.nav.hjelpemidler.delbestilling.delbestilling.Hjelpemiddeldeler
import no.nav.hjelpemidler.delbestilling.delbestilling.PiloterService
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.AnmodningRepository
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.AnmodningService
import no.nav.hjelpemidler.delbestilling.hjelpemidler.HjelpemidlerService
import no.nav.hjelpemidler.delbestilling.infrastructure.email.Email
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.Grunndata
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.GrunndataClient
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Oebs
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.OebsApiProxyClient
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.OebsSinkClient
import no.nav.hjelpemidler.delbestilling.infrastructure.kafka.Kafka
import no.nav.hjelpemidler.delbestilling.infrastructure.metrics.Metrics
import no.nav.hjelpemidler.delbestilling.oppslag.OppslagClient
import no.nav.hjelpemidler.delbestilling.oppslag.GeografiService
import no.nav.hjelpemidler.delbestilling.pdl.PdlClient
import no.nav.hjelpemidler.delbestilling.pdl.PdlService
import no.nav.hjelpemidler.delbestilling.roller.RolleClient
import no.nav.hjelpemidler.delbestilling.roller.RolleService
import no.nav.hjelpemidler.delbestilling.infrastructure.slack.Slack
import no.nav.hjelpemidler.hjelpemidlerdigitalSoknadapi.tjenester.norg.NorgClient
import no.nav.hjelpemidler.hjelpemidlerdigitalSoknadapi.tjenester.norg.NorgService
import no.nav.hjelpemidler.http.openid.azureADClient
import no.nav.tms.token.support.tokendings.exchange.TokendingsServiceBuilder
import kotlin.time.Duration.Companion.seconds


class AppContext {
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val tokendingsService = TokendingsServiceBuilder.buildTokendingsService()

    private val azureClient = azureADClient {
        cache(leeway = 10.seconds) {
            maximumSize = 100
        }
    }

    private val grunndata = Grunndata(GrunndataClient())

    private val rolleClient = RolleClient(tokendingsService)

    private val oebsApiProxyClient = OebsApiProxyClient(azureClient)

    private val oppslagClient = OppslagClient()

    private val kafka = Kafka()

    private val metrics = Metrics(kafka)

    private val oebsSinkClient = OebsSinkClient(kafka)

    private val pdlClient = PdlClient(azureClient)

    private val ds = DatabaseConfig.migratedDataSource

    private val delbestillingRepository = DelbestillingRepository(ds)

    val anmodningRepository = AnmodningRepository(ds)

    private val pdlService = PdlService(pdlClient)

    private val oebs = Oebs(oebsApiProxyClient, oebsSinkClient)

    private val geografiService = GeografiService(oppslagClient)

    val rolleService = RolleService(rolleClient)

    val slack = Slack(delbestillingRepository, backgroundScope)

    val norgClient = NorgClient()

    val norgService = NorgService(norgClient)

    val email = Email()

    val anmodningService = AnmodningService(anmodningRepository, oebs, norgService, slack, email, grunndata)

    val piloterService = PiloterService(norgService)

    val hjelpemiddeldeler = Hjelpemiddeldeler(grunndata)

    val delbestillingService = DelbestillingService(
        delbestillingRepository,
        pdlService,
        oebs,
        geografiService,
        metrics,
        slack,
        grunndata,
        anmodningService,
        piloterService,
        hjelpemiddeldeler
    )

    val hjelpemidlerService = HjelpemidlerService(grunndata, backgroundScope)

    fun shutdown() {
        backgroundScope.cancel("Shutting down application")
    }
}
