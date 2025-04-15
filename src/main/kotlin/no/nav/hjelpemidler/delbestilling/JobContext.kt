package no.nav.hjelpemidler.delbestilling

import no.nav.hjelpemidler.delbestilling.infrastructure.defaultHttpClient
import no.nav.hjelpemidler.http.openid.azureADClient
import kotlin.time.Duration.Companion.seconds


class JobContext {
    val azureClient = azureADClient {
        cache(leeway = 10.seconds) {
            maximumSize = 100
        }
    }

    val client = defaultHttpClient()

}
