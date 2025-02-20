package no.nav.hjelpemidler.delbestilling

import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.delbestilling.azureRoutes
import no.nav.hjelpemidler.delbestilling.delbestilling.delbestillingApiAuthenticated
import no.nav.hjelpemidler.delbestilling.delbestilling.delbestillingApiPublic
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.validerData
import no.nav.hjelpemidler.delbestilling.plugins.medDelbestillerRolle
import no.nav.hjelpemidler.delbestilling.hjelpemidler.hjelpemiddelApi
import no.nav.tms.token.support.azure.validation.AzureAuthenticator
import no.nav.tms.token.support.tokenx.validation.TokenXAuthenticator
import no.nav.tms.token.support.tokenx.validation.user.TokenXUserFactory

fun main(args: Array<String>): Unit = io.ktor.server.cio.EngineMain.main(args)

private val logg = KotlinLogging.logger {}

fun Application.module() {
    validerData()
    configure()
    setupRoutes()
}

fun Application.setupRoutes() {
    val ctx = AppContext()

    routing {
        route("/api") {
            authenticate(TokenXAuthenticator.name) {
                medDelbestillerRolle(ctx.rolleService)

                delbestillingApiAuthenticated(ctx.delbestillingService)
            }

            rateLimit(RateLimitName("public")) {
                delbestillingApiPublic(ctx.delbestillingService)
            }

            authenticate(AzureAuthenticator.name) {
                azureRoutes(ctx.delbestillingService)
            }

            hjelpemiddelApi(ctx.hjelpemidlerService)
        }

        internal()
    }
}

fun ApplicationCall.tokenXUser() = TokenXUserFactory.createTokenXUser(this)
