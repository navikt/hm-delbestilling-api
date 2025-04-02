package no.nav.hjelpemidler.delbestilling

import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingRepository
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.AnmodningRepository
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.AnmodningService
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Oebs
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.OebsApiProxyClient
import no.nav.hjelpemidler.delbestilling.slack.SlackClient
import no.nav.hjelpemidler.hjelpemidlerdigitalSoknadapi.tjenester.norg.NorgClient
import no.nav.hjelpemidler.hjelpemidlerdigitalSoknadapi.tjenester.norg.NorgService
import no.nav.hjelpemidler.http.openid.azureADClient
import kotlin.time.Duration.Companion.seconds


class JobContext {
    private val azureClient = azureADClient {
        cache(leeway = 10.seconds) {
            maximumSize = 100
        }
    }

    private val oebsApiProxyClient = OebsApiProxyClient(azureClient)

    private val ds = Database.migratedDataSource

    private val delbestillingRepository = DelbestillingRepository(ds)

    val anmodningRepository = AnmodningRepository(ds)

    private val oebs = Oebs(oebsApiProxyClient)

    val slackClient = SlackClient(delbestillingRepository)

    val norgClient = NorgClient()

    val norgService = NorgService(norgClient)

    val anmodningService = AnmodningService(anmodningRepository, oebs, norgService, slackClient)
}
