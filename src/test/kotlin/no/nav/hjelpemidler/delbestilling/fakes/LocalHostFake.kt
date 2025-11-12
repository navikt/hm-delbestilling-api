package no.nav.hjelpemidler.delbestilling.fakes

import no.nav.hjelpemidler.delbestilling.infrastructure.leaderElection.LocalHostnameProvider

class LocalHostFake: LocalHostnameProvider {

    var hostname: String? = "localhost"

    override suspend fun hentHostnameOrNull(): String? {
        return hostname
    }
}