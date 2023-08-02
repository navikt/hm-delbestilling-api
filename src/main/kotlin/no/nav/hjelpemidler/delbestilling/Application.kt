package no.nav.hjelpemidler.delbestilling

import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import no.nav.hjelpemidler.delbestilling.delbestilling.azureRoutes
import no.nav.hjelpemidler.delbestilling.delbestilling.delbestillingApi
import no.nav.hjelpemidler.delbestilling.delbestilling.delbestillingApiAuthenticated
import no.nav.hjelpemidler.hjelpemidler.hjelpemidler.hjelpemiddelApi
import no.nav.tms.token.support.authentication.installer.installAuthenticators
import no.nav.tms.token.support.azure.validation.AzureAuthenticator
import no.nav.tms.token.support.azure.validation.installAzureAuth
import no.nav.tms.token.support.tokenx.validation.TokenXAuthenticator
import no.nav.tms.token.support.tokenx.validation.installTokenXAuth

fun main(args: Array<String>): Unit = io.ktor.server.cio.EngineMain.main(args)

fun Application.module() {
    configure()
    setupRoutes()
}

fun Application.setupRoutes() {
    val ctx = AppContext()

    installAuthenticators {
        installAzureAuth {}
        installTokenXAuth {}
    }

    routing {
        route("/api") {
            authenticate(TokenXAuthenticator.name) {
                delbestillingApiAuthenticated(ctx.rolleService, ctx.delbestillingService)
            }

            //authenticate(AzureAuthenticator.name) {
                azureRoutes(ctx.delbestillingService)
            //}

            hjelpemiddelApi(ctx.hjelpemidlerService)

            rateLimit(RateLimitName("public")) {
                delbestillingApi(ctx.delbestillingService)
            }
        }

        internal()
    }
}
