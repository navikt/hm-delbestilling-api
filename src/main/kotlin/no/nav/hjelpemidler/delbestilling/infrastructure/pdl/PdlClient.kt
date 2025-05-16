package no.nav.hjelpemidler.delbestilling.infrastructure.pdl

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.hjelpemidler.delbestilling.config.AppConfig
import no.nav.hjelpemidler.delbestilling.infrastructure.defaultHttpClient
import no.nav.hjelpemidler.delbestilling.infrastructure.navCorrelationId
import no.nav.hjelpemidler.http.openid.OpenIDClient
import no.nav.hjelpemidler.http.openid.bearerAuth


class PdlClient(
    private val openIDClient: OpenIDClient,
    private val client: HttpClient = defaultHttpClient(),
    private val baseUrl: String = AppConfig.PDL_GRAPHQL_URL,
    private val apiScope: String = AppConfig.PDL_API_SCOPE,
) : PdlClientInterface {

    private suspend inline fun <reified T : Any> pdlRequest(pdlQuery: GraphqlQuery): T {
        return withContext(Dispatchers.IO) {
            val tokenSet = openIDClient.grant(apiScope)
            client.post(baseUrl) {
                bearerAuth(tokenSet)
                headers {
                    header("behandlingsnummer", "B653")
                    navCorrelationId()
                }
                setBody(pdlQuery)
            }.body()
        }
    }

    override suspend fun hentKommunenummer(fnummer: String): PdlPersonResponse = pdlRequest(hentKommunenummerQuery(fnummer))

    override suspend fun hentPersonNavn(fnr: String): PdlPersonResponse = pdlRequest(hentPersonNavnQuery(fnr))
}