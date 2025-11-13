package no.nav.hjelpemidler.delbestilling

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.auth.authenticate
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import no.nav.hjelpemidler.delbestilling.config.configure
import no.nav.hjelpemidler.delbestilling.config.isDev
import no.nav.hjelpemidler.delbestilling.config.isProd
import no.nav.hjelpemidler.delbestilling.delbestilling.delbestillingApiAuthenticated
import no.nav.hjelpemidler.delbestilling.devtools.devtoolsApi
import no.nav.hjelpemidler.delbestilling.infrastructure.monitoring.helsesjekkApi
import no.nav.hjelpemidler.delbestilling.infrastructure.security.medDelbestillerRolle
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.data.validerData
import no.nav.hjelpemidler.delbestilling.oppslag.publicApi
import no.nav.hjelpemidler.delbestilling.ordrestatus.ordrestatusRoutes
import no.nav.hjelpemidler.domain.person.TILLAT_SYNTETISKE_FØDSELSNUMRE
import no.nav.tms.token.support.azure.validation.AzureAuthenticator
import no.nav.tms.token.support.tokenx.validation.TokenXAuthenticator

private val log = KotlinLogging.logger {}

fun main() {
    embeddedServer(CIO, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    TILLAT_SYNTETISKE_FØDSELSNUMRE = !isProd()

    val ctx = AppContext()

    validerData()
    configure()
    setupRoutes(ctx)

    monitor.subscribe(ApplicationStarted) {
        log.info { "Applikasjon startet. Starter bakgrunnsjobber." }
        ctx.applicationStarted()
    }

    monitor.subscribe(ApplicationStopped) {
        ctx.shutdown()
    }
}

fun Application.setupRoutes(ctx: AppContext) {
    routing {
        route("/api") {
            authenticate(TokenXAuthenticator.name) {
                medDelbestillerRolle(ctx.roller)
                delbestillingApiAuthenticated(ctx.delbestillingService, ctx.slack)
            }

            authenticate(AzureAuthenticator.name) {
                ordrestatusRoutes(ctx.delbestillingStatusService)
            }

            publicApi(ctx.hjelpemiddeloversikt, ctx.oppslagService)

            if (isDev()) {
                devtoolsApi(ctx.devtools(), ctx.delbestillingService)
            }
        }

        helsesjekkApi()
    }

}
