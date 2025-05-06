package no.nav.hjelpemidler.delbestilling.infrastructure.oebs

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.hjelpemidler.delbestilling.config.AppConfig
import no.nav.hjelpemidler.delbestilling.infrastructure.defaultHttpClient
import no.nav.hjelpemidler.delbestilling.infrastructure.navCorrelationId
import no.nav.hjelpemidler.http.openid.OpenIDClient
import no.nav.hjelpemidler.http.openid.bearerAuth

private val log = KotlinLogging.logger {}

class OebsApiProxyClient(
    private val azureAdClient: OpenIDClient,
    private val client: HttpClient = defaultHttpClient(),
    private val baseUrl: String = AppConfig.OEBS_API_URL,
    private val apiScope: String = AppConfig.OEBS_API_SCOPE,
) : OebsApiProxy {

    private suspend inline fun <reified T> executeRequest(url: String, method: HttpMethod, body: Any? = null): T {
        try {
            return withContext(Dispatchers.IO) {
                val tokenSet = azureAdClient.grant(apiScope)
                val httpResponse = client.request(url) {
                    this.method = method
                    bearerAuth(tokenSet)
                    navCorrelationId()
                    if (body != null) {
                        setBody(body)
                    }
                }
                httpResponse.body()
            }
        } catch (t: Throwable) {
            log.error(t) { "OeBS request feilet: [$method] $url" }
            throw t
        }
    }

    private suspend inline fun <reified T> post(url: String, body: Any? = null): T =
        executeRequest(url, HttpMethod.Post, body)

    override suspend fun hentUtlånPåArtnrOgSerienr(artnr: String, serienr: String): UtlånPåArtnrOgSerienrResponse =
        post("$baseUrl/utlanSerienrArtnr", UtlånPåArtnrOgSerienrRequest(artnr, serienr))

    override suspend fun hentUtlånPåArtnr(artnr: String): List<Utlån> =
        post("$baseUrl/utlanArtnr", artnr)

    override suspend fun hentPersoninfo(fnr: String): List<OebsPersoninfo> =
        post("$baseUrl/getLeveringsaddresse", fnr)

    override suspend fun hentBrukerpassinfo(fnr: String): Brukerpass =
        post("$baseUrl/hent-brukerpass", FnrDto(fnr))

    override suspend fun hentLagerstatusForKommunenummer(kommunenummer: String, hmsnrs: List<String>): List<LagerstatusResponse> =
        post("$baseUrl/lager/sentral/$kommunenummer", LagerstatusRequest(hmsnrs))

    override suspend fun hentLagerstatusForEnhetnr(enhetnr: String, hmsnrs: List<String>): List<LagerstatusResponse> =
        post("$baseUrl/lager/sentral/enhet/$enhetnr", LagerstatusRequest(hmsnrs))
}


