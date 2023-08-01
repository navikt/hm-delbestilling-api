package no.nav.hjelpemidler.delbestilling

import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingRepository
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingService
import no.nav.hjelpemidler.delbestilling.hjelpemidler.HjelpemidlerService
import no.nav.hjelpemidler.delbestilling.kafka.KafkaService
import no.nav.hjelpemidler.delbestilling.oebs.OebsApiProxyClient
import no.nav.hjelpemidler.delbestilling.oebs.OebsService
import no.nav.hjelpemidler.delbestilling.oebs.OebsSinkClient
import no.nav.hjelpemidler.delbestilling.pdl.PdlClient
import no.nav.hjelpemidler.delbestilling.pdl.PdlService
import no.nav.hjelpemidler.delbestilling.roller.RolleClient
import no.nav.hjelpemidler.delbestilling.roller.RolleService
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

    private val rolleClient = RolleClient(tokendingsService)

    private val oebsApiProxyClient = OebsApiProxyClient(azureClient)

    private val kafkaService = KafkaService()

    private val oebsSinkClient = OebsSinkClient(kafkaService)

    private val pdlClient = PdlClient(azureClient)

    private val ds = Database.migratedDataSource

    val delbestillingRepository = DelbestillingRepository(ds)

    val rolleService = RolleService(rolleClient)

    val pdlService = PdlService(pdlClient)

    val oebsService = OebsService(oebsApiProxyClient, oebsSinkClient)

    val delbestillingService = DelbestillingService(delbestillingRepository, pdlService, oebsService)

    val hjelpemidlerService = HjelpemidlerService()
}
