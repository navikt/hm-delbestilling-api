package no.nav.hjelpemidler.delbestilling.roller

class RolleService(
    private val client: RolleClient
) {
    suspend fun hentRolle(token: String): RolleResultat {
        return client.hentRolle(token)
    }
}
