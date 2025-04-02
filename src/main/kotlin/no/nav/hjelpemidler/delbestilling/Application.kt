package no.nav.hjelpemidler.delbestilling

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.coroutines.runBlocking
import no.nav.hjelpemidler.delbestilling.delbestilling.azureRoutes
import no.nav.hjelpemidler.delbestilling.delbestilling.delbestillingApiAuthenticated
import no.nav.hjelpemidler.delbestilling.delbestilling.delbestillingApiPublic
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.validerData
import no.nav.hjelpemidler.delbestilling.hjelpemidler.hjelpemiddelApi
import no.nav.hjelpemidler.delbestilling.plugins.medDelbestillerRolle
import no.nav.hjelpemidler.domain.person.TILLAT_SYNTETISKE_FØDSELSNUMRE
import no.nav.hjelpemidler.delbestilling.slack.log
import no.nav.tms.token.support.azure.validation.AzureAuthenticator
import no.nav.tms.token.support.tokenx.validation.TokenXAuthenticator
import no.nav.tms.token.support.tokenx.validation.user.TokenXUserFactory

private val log = KotlinLogging.logger{}

fun main(args: Array<String>): Unit {
    when (System.getenv("CRONJOB_TYPE")) {
        "RAPPORTER_DELER_TIL_ANMODNING" -> rapporterDelerTilAnmodning()
        else -> io.ktor.server.cio.EngineMain.main(args)
    }
}

fun Application.module() {
    TILLAT_SYNTETISKE_FØDSELSNUMRE = !isProd()

    validerData()
    configure()
    setupRoutes()
}

fun rapporterDelerTilAnmodning() {
    val ctx = AppContext()
    log.info { "Kjører jobb for å rapportere deler til anmodning" }
    runBlocking {
        if (isDev()) {
            log.info { "Resetter deler som er rapportert i dev" }
            ctx.anmodningService.markerDelerSomIkkeRapportert()
        }
        ctx.delbestillingService.rapporterDelerUtenDeking()
    }
}

fun Application.setupRoutes() {
    val ctx = AppContext()

    routing {
        route("/api") {
            authenticate(TokenXAuthenticator.name) {
                medDelbestillerRolle(ctx.rolleService)

                delbestillingApiAuthenticated(ctx.delbestillingService, ctx.slackClient)
            }

            rateLimit(RateLimitName("public")) {
                delbestillingApiPublic(ctx.delbestillingService, ctx.anmodningService)
            }

            authenticate(AzureAuthenticator.name) {
                azureRoutes(ctx.delbestillingService)
            }

            hjelpemiddelApi(ctx.hjelpemidlerService)
        }

        internal()
    }

    // Rapportering().rapporter()
}

fun ApplicationCall.tokenXUser() = TokenXUserFactory.createTokenXUser(this)
