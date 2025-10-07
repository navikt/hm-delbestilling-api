package no.nav.hjelpemidler.delbestilling.infrastructure.geografi

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.hjelpemidler.delbestilling.config.AppConfig
import no.nav.hjelpemidler.delbestilling.infrastructure.defaultHttpClient
import no.nav.hjelpemidler.delbestilling.infrastructure.navCorrelationId


class OppslagClient(
    private val client: HttpClient = defaultHttpClient(),
    private val url: String = AppConfig.OPPSLAG_API_URL,
): OppslagClientInterface {

    override suspend fun hentKommune(kommunenr: String): KommuneDto {
        return withContext(Dispatchers.IO) {
            client.get("$url/api/geografi/kommuner/$kommunenr") {
                headers {
                    navCorrelationId()
                }
            }.body()
        }
    }
}

