package no.nav.hjelpemidler.delbestilling.oppslag

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.hjelpemidler.delbestilling.config.Config
import no.nav.hjelpemidler.delbestilling.infrastructure.defaultHttpClient
import no.nav.hjelpemidler.delbestilling.infrastructure.navCorrelationId

private val log = KotlinLogging.logger { }

class OppslagClient(
    private val client: HttpClient = defaultHttpClient(),
    private val url: String = Config.OPPSLAG_API_URL,
) {

    suspend fun hentKommune(kommunenr: String): KommuneDto {
        return try {
            withContext(Dispatchers.IO) {
                client.get("$url/api/geografi/kommuner/$kommunenr") {
                    headers {
                        navCorrelationId()
                    }
                }.body()
            }
        } catch (e: Exception) {
            log.warn(e) { "Klarte ikke å hente kommune for kommunenr: $kommunenr" }
            throw e
        }
    }
}

data class KommuneDto(
    val fylkesnummer: String,
    val fylkesnavn: String,
    val kommunenummer: String,
    val kommunenavn: String,
)
