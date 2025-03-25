package no.nav.hjelpemidler.delbestilling.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.application.install
import io.ktor.server.auth.AuthenticationChecked
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.util.AttributeKey
import no.nav.hjelpemidler.delbestilling.infrastructure.monitoring.Logg
import no.nav.hjelpemidler.delbestilling.roller.Delbestiller
import no.nav.hjelpemidler.delbestilling.roller.RolleService
import no.nav.hjelpemidler.delbestilling.tokenXUser


class DelbestillerRollePluginConfig {
    lateinit var rolleService: RolleService
}

val delbestillerRolleKey = AttributeKey<Delbestiller>("delbestillerRolleKey")

val DelbestillerRollePlugin = createRouteScopedPlugin(
    name = "DelbestillerRollePlugin",
    createConfiguration = ::DelbestillerRollePluginConfig
) {
    val rolleService = pluginConfig.rolleService

    on(AuthenticationChecked) { call ->
        try {
            val bestiller = call.tokenXUser()

            val resultat = rolleService.hentDelbestillerRolle(bestiller.tokenString)

            if (!resultat.kanBestilleDeler) {
                return@on call.respond(HttpStatusCode.Forbidden, "Du har ikke rettigheter til å gjøre dette")
            }

            call.attributes.put(delbestillerRolleKey, resultat)
            
        } catch (e: Exception) {
            Logg.error(e) { "Kunne ikke sjekke rolle med DelbestillerRollePlugin " }
            throw e
        }
    }
}

fun Route.medDelbestillerRolle(rolleService: RolleService) =
    install(DelbestillerRollePlugin) {
        this.rolleService = rolleService
    }
