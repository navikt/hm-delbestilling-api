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
import no.nav.hjelpemidler.delbestilling.infrastructure.roller.Roller
import no.nav.hjelpemidler.delbestilling.infrastructure.roller.RollerClient
import no.nav.hjelpemidler.delbestilling.infrastructure.slack.Slack
import no.nav.hjelpemidler.hjelpemidlerdigitalSoknadapi.tjenester.norg.NorgClient
import no.nav.hjelpemidler.hjelpemidlerdigitalSoknadapi.tjenester.norg.Norg
import no.nav.hjelpemidler.http.openid.azureADClient
import no.nav.tms.token.support.tokendings.exchange.TokendingsServiceBuilder
import kotlin.time.Duration.Companion.seconds


class AppContext {
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val azureClient = azureADClient {
        cache(leeway = 10.seconds) {
            maximumSize = 100
        }
    }

    private val grunndata = Grunndata(GrunndataClient())

    private val kafka = Kafka()

    private val ds = DatabaseConfig.migratedDataSource

    private val delbestillingRepository = DelbestillingRepository(ds)

    private val oebs = Oebs(OebsApiProxyClient(azureClient), OebsSinkClient(kafka))

    
    val roller = Roller(RollerClient(TokendingsServiceBuilder.buildTokendingsService()))

    val slack = Slack(delbestillingRepository, backgroundScope)

    val norg = Norg(NorgClient())

    val anmodningService = AnmodningService(AnmodningRepository(ds), oebs, norg, slack, Email(), grunndata)

    val piloterService = PiloterService(norg)

    val hjelpemiddeldeler = Hjelpemiddeldeler(grunndata)

    val delbestillingService = DelbestillingService(
        delbestillingRepository,
        Pdl(PdlClient(azureClient)),
        oebs,
        Kommuneoppslag(OppslagClient()),
        Metrics(kafka),
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
