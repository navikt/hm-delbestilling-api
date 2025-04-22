package no.nav.hjelpemidler.delbestilling

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.coroutines.runBlocking
import no.nav.hjelpemidler.configuration.EnvironmentVariable
import no.nav.hjelpemidler.delbestilling.config.configure
import no.nav.hjelpemidler.delbestilling.config.isDev
import no.nav.hjelpemidler.delbestilling.config.isProd
import no.nav.hjelpemidler.delbestilling.delbestilling.azureRoutes
import no.nav.hjelpemidler.delbestilling.delbestilling.delbestillingApiAuthenticated
import no.nav.hjelpemidler.delbestilling.delbestilling.delbestillingApiPublic
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.validerData
import no.nav.hjelpemidler.delbestilling.hjelpemidler.hjelpemiddelApi
import no.nav.hjelpemidler.delbestilling.infrastructure.monitoring.helsesjekkApi
import no.nav.hjelpemidler.delbestilling.infrastructure.security.medDelbestillerRolle
import no.nav.hjelpemidler.delbestilling.infrastructure.slack.log
import no.nav.hjelpemidler.domain.person.TILLAT_SYNTETISKE_FØDSELSNUMRE
import no.nav.hjelpemidler.http.openid.bearerAuth
import no.nav.tms.token.support.azure.validation.AzureAuthenticator
import no.nav.tms.token.support.tokenx.validation.TokenXAuthenticator

private val log = KotlinLogging.logger {}

fun main(args: Array<String>): Unit {
    when (System.getenv("CRONJOB_TYPE")) {
        "RAPPORTER_DELER_TIL_ANMODNING" -> rapporterDelerTilAnmodning()
        else -> io.ktor.server.cio.EngineMain.main(args)
    }
}

fun Application.module() {
    TILLAT_SYNTETISKE_FØDSELSNUMRE = !isProd()

    val ctx = AppContext()

    validerData()
    configure()
    setupRoutes(ctx)
}

fun rapporterDelerTilAnmodning() {
    val ctx = JobContext()
    val DELBESTILLING_API_URL by EnvironmentVariable
    val DELBESTILLING_API_SCOPE by EnvironmentVariable

    log.info { "Kjører jobb for å rapportere deler til anmodning" }

    runBlocking {
        if (isDev()) {
            log.info { "Resetter deler som er rapportert i dev" }
            ctx.client.delete("${DELBESTILLING_API_URL}/api/rapporter-deler-uten-dekning")
        }

        val tokenSet = ctx.azureClient.grant(DELBESTILLING_API_SCOPE)
        ctx.client.post("${DELBESTILLING_API_URL}/api/anmodning/rapporter-deler-til-anmodning") {
            bearerAuth(tokenSet)
        }
    }
}

fun Application.setupRoutes(ctx: AppContext) {
    routing {
        route("/api") {
            authenticate(TokenXAuthenticator.name) {
                medDelbestillerRolle(ctx.rolleService)

                delbestillingApiAuthenticated(ctx.delbestillingService, ctx.slack)
            }

            rateLimit(RateLimitName("public")) {
                delbestillingApiPublic(ctx.delbestillingService, ctx.anmodningService)
            }

            authenticate(AzureAuthenticator.name) {
                azureRoutes(ctx.delbestillingService)
            }

            hjelpemiddelApi(ctx.hjelpemidlerService)
        }

        helsesjekkApi()
    }
}
