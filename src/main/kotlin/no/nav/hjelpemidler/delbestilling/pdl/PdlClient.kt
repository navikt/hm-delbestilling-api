package no.nav.hjelpemidler.delbestilling.pdl

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.hjelpemidler.delbestilling.config.Config
import no.nav.hjelpemidler.delbestilling.infrastructure.defaultHttpClient
import no.nav.hjelpemidler.delbestilling.infrastructure.monitoring.PdlRequestFailedException
import no.nav.hjelpemidler.delbestilling.infrastructure.monitoring.PdlResponseMissingData
import no.nav.hjelpemidler.delbestilling.infrastructure.monitoring.PersonNotAccessibleInPdl
import no.nav.hjelpemidler.delbestilling.infrastructure.monitoring.PersonNotFoundInPdl
import no.nav.hjelpemidler.delbestilling.infrastructure.navCorrelationId
import no.nav.hjelpemidler.http.openid.OpenIDClient
import no.nav.hjelpemidler.http.openid.bearerAuth

private val log = KotlinLogging.logger {}
private val secureLog = KotlinLogging.logger("tjenestekall")

class PdlClient(
    private val azureAdClient: OpenIDClient,
    private val client: HttpClient = defaultHttpClient(),
    private val baseUrl: String = Config.PDL_GRAPHQL_URL,
    private val apiScope: String = Config.PDL_API_SCOPE,
) {

    suspend fun hentKommunenummer(fnummer: String): String {
        val response = pdlRequest<PdlPersonResponse>(hentKommunenummerQuery(fnummer))
        validerPdlOppslag(response, validerAdressebeskyttelse = true)
        loggAdvarsler(response)
        secureLog.info{"PDLResponse i hentKommunenummer: $response"}
        return getKommunenr(response)
    }

    private fun getKommunenr(response: PdlPersonResponse): String {
        return response.data?.hentPerson?.bostedsadresse?.get(0)?.vegadresse?.kommunenummer
            ?: throw PdlResponseMissingData("Klarte ikke å finne kommunenummer")
    }

    private fun validerPdlOppslag(pdlPersonResponse: PdlPersonResponse, validerAdressebeskyttelse: Boolean) {
        if (pdlPersonResponse.harFeilmeldinger()) {
            val feilmeldinger = pdlPersonResponse.feilmeldinger()
            if (pdlPersonResponse.feilType() == PdlFeiltype.IKKE_FUNNET) {
                throw PersonNotFoundInPdl("Fant ikke person i PDL $feilmeldinger")
            } else {
                throw PdlRequestFailedException(feilmeldinger)
            }
        } else if (validerAdressebeskyttelse && pdlPersonResponse.harDiskresjonskode()) {
            throw PersonNotAccessibleInPdl()
        }
    }

    suspend fun hentPersonNavn(fnr: String, validerAdressebeskyttelse: Boolean): PdlPersonResponse {
        val response: PdlPersonResponse = pdlRequest(hentPersonNavnQuery(fnr))
        loggAdvarsler(response)
        validerPdlOppslag(response, validerAdressebeskyttelse)
        return response
    }

    private suspend inline fun <reified T : Any> pdlRequest(pdlQuery: GraphqlQuery): T {
        return withContext(Dispatchers.IO) {
            val tokenSet = azureAdClient.grant(apiScope)
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

    private fun loggAdvarsler(response: PdlPersonResponse) {
        response.extensions?.warnings?.forEach { pdlWarning ->
            if (pdlWarning.details.isNullOrEmpty()) {
                log.warn { pdlWarning.message }
            } else {
                log.warn { "${pdlWarning.message} (detaljer: ${pdlWarning.details})" }
            }
        }
    }
}

data class PdlPersonResponse(
    val errors: List<PdlError> = emptyList(),
    val data: PdlHentPerson?,
    val extensions: PdlExtensions? = null,
)

data class PdlExtensions(
    val warnings: List<PdlWarning> = emptyList(),
)

data class PdlWarning(
    val query: String,
    val id: String,
    val message: String,
    val details: String?,
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
    val etternavn: String,
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
