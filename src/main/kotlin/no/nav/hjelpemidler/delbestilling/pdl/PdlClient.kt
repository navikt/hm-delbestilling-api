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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.hjelpemidler.delbestilling.Config
import no.nav.hjelpemidler.delbestilling.exceptions.PdlRequestFailedException
import no.nav.hjelpemidler.delbestilling.exceptions.PdlResponseMissingData
import no.nav.hjelpemidler.delbestilling.exceptions.PersonNotAccessibleInPdl
import no.nav.hjelpemidler.delbestilling.exceptions.PersonNotFoundInPdl
import no.nav.hjelpemidler.http.createHttpClient
import no.nav.hjelpemidler.http.openid.OpenIDClient
import no.nav.hjelpemidler.http.openid.bearerAuth
import java.util.UUID

class PdlClient(
    private val azureAdClient: OpenIDClient,
    engine: HttpClientEngine = CIO.create(),
    private val baseUrl: String = Config.PDL_GRAPHQL_URL,
    private val apiScope: String = Config.PDL_API_SCOPE,
) {

    private val client = createHttpClient(engine = engine) {
        expectSuccess = true
        install(HttpRequestRetry) {
            retryOnExceptionOrServerErrors(maxRetries = 5)
            exponentialDelay()
        }
        defaultRequest {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun hentKommunenummer(fnummer: String): String {
        val response = pdlRequest<PdlPersonResponse>(hentKommunenummerQuery(fnummer))
        validerPdlOppslag(response)
        return getKommunenr(response)
    }

    private fun getKommunenr(response: PdlPersonResponse): String {
        return response.data?.hentPerson?.bostedsadresse?.get(0)?.vegadresse?.kommunenummer
            ?: throw PdlResponseMissingData("Klarte ikke å finne kommunenummer")
    }

    private fun validerPdlOppslag(pdlPersonResponse: PdlPersonResponse) {
        if (pdlPersonResponse.harFeilmeldinger()) {
            val feilmeldinger = pdlPersonResponse.feilmeldinger()
            if (pdlPersonResponse.feilType() == PdlFeiltype.IKKE_FUNNET) {
                throw PersonNotFoundInPdl("Fant ikke person i PDL $feilmeldinger")
            } else {
                throw PdlRequestFailedException(feilmeldinger)
            }
        } else if (pdlPersonResponse.harDiskresjonskode()) {
            throw PersonNotAccessibleInPdl()
        }
    }

    suspend fun hentPersonNavn(fnr: String) = getPersonNavn(pdlRequest(hentPersonNavnQuery(fnr)))

    private fun getPersonNavn(response: PdlPersonResponse): String {
        val navneData = response.data?.hentPerson?.navn?.get(0)
            ?: throw PdlResponseMissingData("PDL response mangler data")
        val mellomnavn = navneData.mellomnavn ?: ""
        return "${navneData.fornavn} $mellomnavn ${navneData.etternavn}"
    }


    private suspend inline fun <reified T : Any> pdlRequest(pdlQuery: GraphqlQuery): T {
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

data class PdlPersonResponse(
    val errors: List<PdlError> = emptyList(),
    val data: PdlHentPerson?,
)

data class PdlHentPerson(
    val hentPerson: PdlPerson?,
)

fun PdlPersonResponse.feilType(): PdlFeiltype {
    return if (this.errors.map { it.extensions.code }
            .contains("not_found")
    ) {
        PdlFeiltype.IKKE_FUNNET
    } else {
        PdlFeiltype.TEKNISK_FEIL
    }
}

enum class PdlFeiltype {
    IKKE_FUNNET,
    TEKNISK_FEIL,
}

fun PdlPersonResponse.harFeilmeldinger(): Boolean {
    return this.errors.isNotEmpty()
}

fun PdlHentPerson.isKode6Or7(): Boolean {
    val adressebeskyttelse = this.hentPerson?.adressebeskyttelse
    return if (adressebeskyttelse.isNullOrEmpty()) {
        false
    } else {
        return adressebeskyttelse.any {
            it.isKode6() || it.isKode7()
        }
    }
}

fun Adressebeskyttelse.isKode6(): Boolean {
    return this.gradering == Gradering.STRENGT_FORTROLIG || this.gradering == Gradering.STRENGT_FORTROLIG_UTLAND
}

fun Adressebeskyttelse.isKode7(): Boolean {
    return this.gradering == Gradering.FORTROLIG
}

fun PdlPersonResponse.harDiskresjonskode(): Boolean = if (this.data == null) {
    false
} else {
    this.data.isKode6Or7()
}

fun PdlPersonResponse.feilmeldinger(): String {
    return this.errors.joinToString(",") { "${it.message}. Type ${it.extensions.classification}:${it.extensions.code}" }
}

data class PdlPerson(
    val navn: List<PdlPersonNavn> = emptyList(),
    val bostedsadresse: List<Bostedsadresse> = emptyList(),
    val adressebeskyttelse: List<Adressebeskyttelse>? = emptyList(),
)

data class Adressebeskyttelse(
    val gradering: Gradering,
)

enum class Gradering {
    STRENGT_FORTROLIG_UTLAND,
    STRENGT_FORTROLIG,
    FORTROLIG,
    UGRADERT,
}

data class PdlPersonNavn(
    val fornavn: String,
    val mellomnavn: String? = null,
    val etternavn: String
)

data class Bostedsadresse(val vegadresse: Vegadresse?)

data class Vegadresse(
    val kommunenummer: String? = null,
)

data class PdlError(
    val message: String,
    val locations: List<PdlErrorLocation> = emptyList(),
    val path: List<String>? = emptyList(),
    val extensions: PdlErrorExtension,
)

data class PdlErrorLocation(
    val line: Int?,
    val column: Int?,
)

data class PdlErrorExtension(
    val code: String?,
    val classification: String,
)
