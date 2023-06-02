package no.nav.hjelpemidler.delbestilling.pdl

import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.hjelpemidler.delbestilling.Config
import no.nav.hjelpemidler.http.createHttpClient
import no.nav.hjelpemidler.http.openid.OpenIDClient
import no.nav.hjelpemidler.http.openid.bearerAuth

class PdlClient(
    private val azureAdClient: OpenIDClient,
    engine: HttpClientEngine = CIO.create(),
    private val baseUrl: String = Config.PDL_GRAPHQL_URL,
    private val apiScope: String = Config.PDL_API_SCOPE,
) {

    private val client = createHttpClient(engine = engine) {
        expectSuccess = false
        install(HttpRequestRetry) {
            retryOnExceptionOrServerErrors(maxRetries = 5)
            exponentialDelay()
        }
        defaultRequest {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun hentKommunenummer(fnummer: String) = getKommunenr(pdlRequest(hentKommunenummerQuery(fnummer)))

    private fun getKommunenr(response: PdlPersonResponse) =
        response.data?.hentPerson?.bostedsadresse?.get(0)?.vegadresse?.kommunenummer ?: throw PdlRequestFailedException(
            "PDL response mangler data"
        )

    private suspend inline fun <reified T : Any> pdlRequest(pdlQuery: HentKommunenummerGraphqlQuery): T {
        return withContext(Dispatchers.IO) {
            val tokenSet = azureAdClient.grant(apiScope)
            client.post(baseUrl) {
                bearerAuth(tokenSet)
                headers {
                    header("Tema", "HJE")
                    header("X-Correlation-ID", UUID.randomUUID().toString())
                }
                setBody(pdlQuery)
            }.body()
        }
    }
}

data class HentKommunenummerGraphqlQuery(
    val query: String,
    val variables: Variables
)

data class Variables(
    val ident: String
)

fun hentKommunenummerQuery(fnummer: String): HentKommunenummerGraphqlQuery {
    val query = HentKommunenummerGraphqlQuery::class.java.getResource("/pdl/hentKommunenummer.graphql").readText()
        .replace("[\n\r]", "").replace("[\n]", "")
    return HentKommunenummerGraphqlQuery(query, Variables(ident = fnummer))
}

data class PdlPersonResponse(
    val errors: List<PdlError> = emptyList(),
    val data: PdlHentPerson?
)

data class PdlHentPerson(
    val hentPerson: PdlPerson?,
)

data class PdlPerson(
    val bostedsadresse: List<Bostedsadresse> = emptyList(),
)

data class Bostedsadresse(val vegadresse: Vegadresse?)

data class Vegadresse(
    val kommunenummer: String? = null,
)

data class PdlError(
    val message: String,
    val locations: List<PdlErrorLocation> = emptyList(),
    val path: List<String>? = emptyList(),
    val extensions: PdlErrorExtension
)

data class PdlErrorLocation(
    val line: Int?,
    val column: Int?
)

data class PdlErrorExtension(
    val code: String?,
    val classification: String
)

class PdlRequestFailedException(message: String = "") : RuntimeException("Request to PDL Failed $message")
