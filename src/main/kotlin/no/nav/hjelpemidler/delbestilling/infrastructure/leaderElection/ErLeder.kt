package no.nav.hjelpemidler.delbestilling.infrastructure.leaderElection

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress

private val log = KotlinLogging.logger { }

class ErLeder(
    private val elector: Elector
) {

    suspend operator fun invoke(): Boolean {
        val leder = elector.hentLederHostname()
        val hostname = withContext(Dispatchers.IO) {
            InetAddress.getLocalHost()
        }.hostName

        log.info { "leder=$leder, hostname=$hostname" }

        return leder == hostname
    }
}