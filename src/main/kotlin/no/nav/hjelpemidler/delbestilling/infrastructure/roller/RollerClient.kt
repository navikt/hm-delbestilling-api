package no.nav.hjelpemidler.delbestilling.infrastructure.roller

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.headers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.hjelpemidler.delbestilling.config.Config
import no.nav.hjelpemidler.delbestilling.infrastructure.defaultHttpClient
import no.nav.hjelpemidler.delbestilling.infrastructure.navCorrelationId
import no.nav.tms.token.support.tokendings.exchange.TokendingsService


class RollerClient(
    private val tokendingsService: TokendingsService,
    private val client: HttpClient = defaultHttpClient(),
    private val url: String = Config.ROLLER_API_URL,
    private val scope: String = Config.ROLLER_API_SCOPE
) {

    suspend fun hentDelbestillerRolle(token: String): DelbestillerResponse {
        val exchangedToken = tokendingsService.exchangeToken(token, scope)

        return withContext(Dispatchers.IO) {
            client.get("$url/api/delbestiller") {
                headers {
                    header("Authorization", "Bearer $exchangedToken")
                    navCorrelationId()
                }
            }.body()
        }
    }
}
