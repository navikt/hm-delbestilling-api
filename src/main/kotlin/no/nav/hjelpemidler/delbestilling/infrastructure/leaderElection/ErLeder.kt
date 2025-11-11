package no.nav.hjelpemidler.delbestilling.infrastructure.leaderElection


class ErLeder(
    private val elector: Elector,
    private val localHost: LocalHostnameProvider,
) {

    suspend operator fun invoke(): Boolean {
        val lederHostname = elector.hentLedersHostname()
        val localHostname = localHost.hentHostnameOrNull() ?: return false

        return lederHostname.trim().equals(localHostname.trim(), ignoreCase = true)
    }

}