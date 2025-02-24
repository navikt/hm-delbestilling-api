package no.nav.hjelpemidler.delbestilling

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingRepository
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingService
import no.nav.hjelpemidler.delbestilling.hjelpemidler.HjelpemidlerService
import no.nav.hjelpemidler.delbestilling.kafka.KafkaService
import no.nav.hjelpemidler.delbestilling.metrics.Metrics
import no.nav.hjelpemidler.delbestilling.oebs.OebsApiProxyClient
import no.nav.hjelpemidler.delbestilling.oebs.OebsService
import no.nav.hjelpemidler.delbestilling.oebs.OebsSinkClient
import no.nav.hjelpemidler.delbestilling.oppslag.OppslagClient
import no.nav.hjelpemidler.delbestilling.oppslag.OppslagService
import no.nav.hjelpemidler.delbestilling.pdl.PdlClient
import no.nav.hjelpemidler.delbestilling.pdl.PdlService
import no.nav.hjelpemidler.delbestilling.roller.RolleClient
import no.nav.hjelpemidler.delbestilling.roller.RolleService
import no.nav.hjelpemidler.http.openid.azureADClient
import no.nav.tms.token.support.tokendings.exchange.TokendingsServiceBuilder
import kotlin.time.Duration.Companion.seconds

private val logger = KotlinLogging.logger {}

class AppContext {
    private val tokendingsService = TokendingsServiceBuilder.buildTokendingsService()

    private val azureClient = azureADClient {
        cache(leeway = 10.seconds) {
            maximumSize = 100
        }
    }

    private val rolleClient = RolleClient(tokendingsService)

    private val oebsApiProxyClient = OebsApiProxyClient(azureClient)

    private val oppslagClient = OppslagClient()

    private val kafkaService = KafkaService()

    private val metrics = Metrics(kafkaService)

    private val oebsSinkClient = OebsSinkClient(kafkaService)

    private val pdlClient = PdlClient(azureClient)

    private val ds = Database.migratedDataSource

    private val delbestillingRepository = DelbestillingRepository(ds)

    private val pdlService = PdlService(pdlClient)

    private val oebsService = OebsService(oebsApiProxyClient, oebsSinkClient)

    private val oppslagService = OppslagService(oppslagClient)

    val rolleService = RolleService(rolleClient)

    val delbestillingService = DelbestillingService(
        delbestillingRepository,
        pdlService,
        oebsService,
        oppslagService,
        metrics
    )

    val hjelpemidlerService = HjelpemidlerService()
}
