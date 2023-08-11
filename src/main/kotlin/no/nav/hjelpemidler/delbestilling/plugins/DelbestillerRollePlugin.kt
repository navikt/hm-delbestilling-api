package no.nav.hjelpemidler.delbestilling.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.application.install
import io.ktor.server.auth.AuthenticationChecked
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import mu.KotlinLogging
import no.nav.hjelpemidler.delbestilling.roller.RolleService
import no.nav.hjelpemidler.delbestilling.tokenXUser

private val log = KotlinLogging.logger {}

class DelbestillerRollePluginConfig {
    lateinit var rolleService: RolleService
}

val DelbestillerRollePlugin = createRouteScopedPlugin(
    name = "DelbestillerRollePlugin",
    createConfiguration = ::DelbestillerRollePluginConfig
) {
    val rolleService = pluginConfig.rolleService

    on(AuthenticationChecked) { call ->
        try {
            val bestiller = call.tokenXUser()

            val resultat = rolleService.hentDelbestillerRolle(bestiller.tokenString)
            log.info { "resultat: $resultat" }

            if (!resultat.kanBestilleDeler) {
                call.respond(HttpStatusCode.Forbidden, "Du har ikke rettigheter til å gjøre dette")
            }
        } catch (e: Exception) {
            log.error(e) { "Kunne ikke rolle med AuthorizationPlugin " }
            throw e
        }
    }
}

fun Route.medDelbestillerRolle(rolleService: RolleService) =
    install(DelbestillerRollePlugin) {
        this.rolleService = rolleService
    }
