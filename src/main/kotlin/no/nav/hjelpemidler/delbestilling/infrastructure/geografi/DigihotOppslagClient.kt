package no.nav.hjelpemidler.delbestilling.infrastructure.geografi

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.hjelpemidler.delbestilling.config.Config
import no.nav.hjelpemidler.delbestilling.infrastructure.defaultHttpClient
import no.nav.hjelpemidler.delbestilling.infrastructure.navCorrelationId


class DigihotOppslagClient(
    private val client: HttpClient = defaultHttpClient(),
    private val url: String = Config.OPPSLAG_API_URL,
) {

    suspend fun hentKommune(kommunenr: String): KommuneDto {
        return withContext(Dispatchers.IO) {
            client.get("$url/api/geografi/kommuner/$kommunenr") {
                headers {
                    navCorrelationId()
                }
            }.body()
        }
    }
}