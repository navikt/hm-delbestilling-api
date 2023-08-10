package no.nav.hjelpemidler.delbestilling.plugins

import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.auth.AuthenticationChecked
import mu.KotlinLogging
import no.nav.hjelpemidler.delbestilling.AppContext
import no.nav.hjelpemidler.delbestilling.tokenXUser

private val log = KotlinLogging.logger {}

val AuthorizationPlugin = createRouteScopedPlugin(
    name = "AuthorizationPlugin",
) {
    pluginConfig.apply {
        on(AuthenticationChecked) { call ->

            // TODO: er dette OK?
            val ctx = AppContext()
            val rolleService = ctx.rolleService

            val bestiller = call.tokenXUser()
            val bestillerFnr = bestiller.ident

            val resultat = rolleService.hentDelbestillerRolle(bestiller.tokenString)

            log.info { "delbestillerrolle resultat: $resultat" }

            log.info { "bestillerFnr: $bestillerFnr" }
        }
    }
}
