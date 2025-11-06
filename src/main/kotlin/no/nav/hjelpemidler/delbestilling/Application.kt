package no.nav.hjelpemidler.delbestilling

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.auth.authenticate
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.coroutines.runBlocking
import no.nav.hjelpemidler.delbestilling.config.configure
import no.nav.hjelpemidler.delbestilling.config.isDev
import no.nav.hjelpemidler.delbestilling.config.isProd
import no.nav.hjelpemidler.delbestilling.delbestilling.delbestillingApiAuthenticated
import no.nav.hjelpemidler.delbestilling.devtools.devtoolsApi
import no.nav.hjelpemidler.delbestilling.infrastructure.monitoring.helsesjekkApi
import no.nav.hjelpemidler.delbestilling.infrastructure.security.medDelbestillerRolle
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.data.validerData
import no.nav.hjelpemidler.delbestilling.oppslag.oppslagApi
import no.nav.hjelpemidler.delbestilling.ordrestatus.ordrestatusRoutes
import no.nav.hjelpemidler.delbestilling.rapportering.rapporteringRoutes
import no.nav.hjelpemidler.domain.person.TILLAT_SYNTETISKE_FØDSELSNUMRE
import no.nav.hjelpemidler.http.openid.bearerAuth
import no.nav.tms.token.support.azure.validation.AzureAuthenticator
import no.nav.tms.token.support.tokenx.validation.TokenXAuthenticator

private val log = KotlinLogging.logger {}

fun main() {
    when (System.getenv("CRONJOB_TYPE")) {
        "RAPPORTERINGSJOBB" -> triggRapporteringsjobber()
        else -> embeddedServer(CIO, port = 8080, module = Application::module).start(wait = true)
    }
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

/**
 * Rapporteringsjobben kaller på selve appen (hm-delbestilling-api) for å trigge rapporteringene.
 * Siden jobben kun kjøres som 1 instans, kjøres det kun 1 rapporteringsjobb (selv om hm-delbestilling-api har
 * flere instanser).
 * Vi lar hm-delbestilling-api ta seg av selve rapporteringen for å unngå å måtte duplisere logikk/kode
 * og unngå å sjonglere tilgang til databasen.
 */
fun triggRapporteringsjobber() {
    val ctx = JobContext()

    log.info { "Kjører jobb for å rapportere deler til anmodning" }

    runBlocking {

        /*
        if (isDev()) {
            log.info { "Resetter deler som er behandlet i dev" }
            ctx.client.delete("${ctx.DELBESTILLING_API_URL}/api/rapporter-deler-uten-dekning")
        }
         */

        ctx.client.post("${ctx.DELBESTILLING_API_URL}/api/rapportering/start") {
            bearerAuth(ctx.azureClient.grant(ctx.DELBESTILLING_API_SCOPE))
        }
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
                rapporteringRoutes(ctx.rapporteringService)
                ordrestatusRoutes(ctx.delbestillingStatusService)
            }

            rateLimit(RateLimitName("public")) {
                oppslagApi(ctx.hjelpemiddeloversikt, ctx.oppslagService)
            }

            if (isDev()) {
                devtoolsApi(ctx.devtools(), ctx.delbestillingService)
            }
        }

        helsesjekkApi()
    }

}
