package no.nav.hjelpemidler.delbestilling.fakes

import no.nav.hjelpemidler.delbestilling.infrastructure.leaderElection.Elector

class ElectorFake: Elector {
    var leder: String = "localhost"
    override suspend fun hentLedersHostname(): String {
        return leder
    }
}