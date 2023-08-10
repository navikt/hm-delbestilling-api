package no.nav.hjelpemidler.delbestilling.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.auth.AuthenticationChecked
import io.ktor.server.response.respond
import mu.KotlinLogging
import no.nav.hjelpemidler.delbestilling.AppContext
import no.nav.hjelpemidler.delbestilling.tokenXUser

private val log = KotlinLogging.logger {}

val DelbestillerRollePlugin = createRouteScopedPlugin(
    name = "DelbestillerRollePlugin",
) {
    pluginConfig.apply {
        on(AuthenticationChecked) { call ->
            try {
                val ctx = AppContext()
                val rolleService = ctx.rolleService

                val bestiller = call.tokenXUser()

                val resultat = rolleService.hentDelbestillerRolle(bestiller.tokenString)

                if (!resultat.kanBestilleDeler) {
                    call.respond(HttpStatusCode.Forbidden, "Du har ikke rettigheter til å gjøre dette")
                }
            } catch (e: Exception) {
                log.error(e) { "Kunne ikke rolle med AuthorizationPlugin " }
                throw e
            }
        }
    }
}
