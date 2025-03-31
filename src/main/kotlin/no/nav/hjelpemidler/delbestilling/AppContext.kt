package no.nav.hjelpemidler.delbestilling

import no.nav.hjelpemidler.delbestilling.config.DatabaseConfig
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingRepository
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingService
import no.nav.hjelpemidler.delbestilling.hjelpemidler.HjelpemidlerService
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.Grunndata
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.GrunndataClient
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Oebs
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.OebsApiProxyClient
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.OebsSink
import no.nav.hjelpemidler.delbestilling.infrastructure.kafka.Kafka
import no.nav.hjelpemidler.delbestilling.infrastructure.monitoring.Metrics
import no.nav.hjelpemidler.delbestilling.infrastructure.geografi.DigihotOppslagClient
import no.nav.hjelpemidler.delbestilling.infrastructure.geografi.Kommuneoppslag
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.PdlClient
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.Pdl
import no.nav.hjelpemidler.delbestilling.roller.RolleClient
import no.nav.hjelpemidler.delbestilling.roller.RolleService
import no.nav.hjelpemidler.delbestilling.slack.SlackClient
import no.nav.hjelpemidler.http.openid.azureADClient
import no.nav.tms.token.support.tokendings.exchange.TokendingsServiceBuilder
import kotlin.time.Duration.Companion.seconds


class AppContext {
    private val tokendingsService = TokendingsServiceBuilder.buildTokendingsService()

    private val azureClient = azureADClient {
        cache(leeway = 10.seconds) {
            maximumSize = 100
        }
    }

    private val grunndata = Grunndata(GrunndataClient())

    private val rolleClient = RolleClient(tokendingsService)

    private val oebsApiProxyClient = OebsApiProxyClient(azureClient)

    private val digihotOppslagClient = DigihotOppslagClient()

    private val kafka = Kafka()

    private val metrics = Metrics(kafka)

    private val oebsSinkClient = OebsSink(kafka)

    private val pdlClient = PdlClient(azureClient)

    private val ds = DatabaseConfig.migratedDataSource

    private val delbestillingRepository = DelbestillingRepository(ds)

    private val pdl = Pdl(pdlClient)

    private val oebs = Oebs(oebsApiProxyClient, oebsSinkClient)

    private val kommuneoppslag = Kommuneoppslag(digihotOppslagClient)

    val rolleService = RolleService(rolleClient)

    val slackClient = SlackClient(delbestillingRepository)

    val delbestillingService = DelbestillingService(
        delbestillingRepository,
        pdl,
        oebs,
        kommuneoppslag,
        metrics,
        slackClient,
        grunndata,
    )

    val hjelpemidlerService = HjelpemidlerService(grunndata)
}
