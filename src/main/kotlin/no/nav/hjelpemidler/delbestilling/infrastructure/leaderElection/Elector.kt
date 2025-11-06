package no.nav.hjelpemidler.delbestilling.infrastructure.leaderElection

interface Elector {
    suspend fun hentLederHostname(): String
}