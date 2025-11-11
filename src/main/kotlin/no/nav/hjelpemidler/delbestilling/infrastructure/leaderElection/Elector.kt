package no.nav.hjelpemidler.delbestilling.infrastructure.leaderElection

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.hjelpemidler.delbestilling.config.AppConfig
import no.nav.hjelpemidler.delbestilling.infrastructure.defaultHttpClient

interface Elector {
    suspend fun hentLedersHostname(): String
}

class ElectorClient(
    private val client: HttpClient = defaultHttpClient(),
    private val url: String = AppConfig.ELECTOR_GET_URL,
) : Elector {

    override suspend fun hentLedersHostname(): String {
        return withContext(Dispatchers.IO) {
            val leder: LeaderResponse = client.get(url).body()
            leder.name
        }

    }
}

data class LeaderResponse(
    val name: String,
)