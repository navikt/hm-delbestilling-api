package no.nav.hjelpemidler.hjelpemidlerdigitalSoknadapi.tjenester.norg

import com.github.benmanes.caffeine.cache.Caffeine
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import no.nav.hjelpemidler.delbestilling.config.AppConfig
import no.nav.hjelpemidler.delbestilling.infrastructure.defaultHttpClient
import no.nav.hjelpemidler.delbestilling.infrastructure.navCorrelationId
import no.nav.hjelpemidler.delbestilling.infrastructure.norg.NorgClientInterface
import java.util.concurrent.TimeUnit


class NorgClient(
    private val client: HttpClient = defaultHttpClient(),
    private val baseUrl: String = AppConfig.NORG_API_URL,
) : NorgClientInterface {

    private val cache = Caffeine.newBuilder()
        .expireAfterWrite(7, TimeUnit.DAYS)
        .maximumSize(400)
        .build<String, Deferred<List<ArbeidsfordelingEnhet>>>()

    override suspend fun hentArbeidsfordelingenheter(kommunenummer: String): List<ArbeidsfordelingEnhet> = cache.get("arbeidsfordelingenheter_for_kommunenr_$kommunenummer") {
        CoroutineScope(Dispatchers.IO).async {
            client.post("$baseUrl/arbeidsfordeling/enheter/bestmatch") {
                navCorrelationId()
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
                setBody(
                    mapOf(
                        "geografiskOmraade" to kommunenummer,
                        "tema" to "HJE",
                        "temagruppe" to "HJLPM",
                    ),
                )
            }.body<List<ArbeidsfordelingEnhet>>()
        }
    }.await()
}
