package no.nav.hjelpemidler.delbestilling.infrastructure.leaderElection

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.UnknownHostException

interface LocalHostnameProvider {
    suspend fun hentHostnameOrNull(): String?
}

private val log = KotlinLogging.logger { }

class LocalHost : LocalHostnameProvider {
    override suspend fun hentHostnameOrNull(): String? {
        return try {
            withContext(Dispatchers.IO) {
                InetAddress.getLocalHost().hostName
            }
        } catch (e: UnknownHostException) {
            log.error(e) { "Klarte ikke å hente lokalt hostname" }
            null
        }
    }
}