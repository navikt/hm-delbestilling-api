package no.nav.hjelpemidler.delbestilling.roller

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.headers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.hjelpemidler.delbestilling.Config
import no.nav.hjelpemidler.delbestilling.infrastructure.defaultHttpClient
import no.nav.hjelpemidler.delbestilling.infrastructure.navCorrelationId
import no.nav.tms.token.support.tokendings.exchange.TokendingsService

private val logger = KotlinLogging.logger { }

class RolleClient(
    private val tokendingsService: TokendingsService,
    private val client: HttpClient = defaultHttpClient(),
    private val url: String = Config.ROLLER_API_URL,
    private val scope: String = Config.ROLLER_API_SCOPE
) {

    suspend fun hentDelbestillerRolle(token: String): DelbestillerResponse {
        val exchangedToken = tokendingsService.exchangeToken(token, scope)

        return try {
            withContext(Dispatchers.IO) {
                client.get("$url/api/delbestiller") {
                    headers {
                        header("Authorization", "Bearer $exchangedToken")
                        navCorrelationId()
                    }
                }.body()
            }
        } catch (e: Exception) {
            logger.error(e) { "Henting av delbestillerrolle feilet" }
            throw e
        }
    }
}

data class Delbestiller(
    val kanBestilleDeler: Boolean,
    val kommunaleOrgs: List<Organisasjon>,
    val erKommunaltAnsatt: Boolean,
    val godkjenteIkkeKommunaleOrgs: List<Organisasjon>,
    val erAnsattIGodkjentIkkeKommunaleOrgs: Boolean,
)

data class DelbestillerResponse(
    val delbestillerrolle: Delbestiller
)
