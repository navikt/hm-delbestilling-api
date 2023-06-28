package no.nav.hjelpemidler.delbestilling

import com.fasterxml.jackson.databind.DeserializationFeature
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.IgnoreTrailingSlash
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import no.nav.hjelpemidler.delbestilling.delbestilling.delbestillingApi
import no.nav.hjelpemidler.delbestilling.delbestilling.delbestillingApiAuthenticated
import no.nav.hjelpemidler.delbestilling.exceptions.configureStatusPages
import no.nav.hjelpemidler.hjelpemidler.hjelpemidler.hjelpemiddelApi
import no.nav.tms.token.support.tokenx.validation.TokenXAuthenticator
import no.nav.tms.token.support.tokenx.validation.installTokenXAuth
import no.nav.tms.token.support.tokenx.validation.mock.SecurityLevel
import no.nav.tms.token.support.tokenx.validation.mock.installTokenXAuthMock
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.TimeZone

fun main(args: Array<String>): Unit = io.ktor.server.cio.EngineMain.main(args)

fun Application.module() {
    configure()
    setupRoutes()
}

fun Application.configure() {
    TimeZone.setDefault(TimeZone.getTimeZone("Europe/Oslo"))

    install(ContentNegotiation) {
        jackson {
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }
    }

    install(IgnoreTrailingSlash)
}

fun Application.setupRoutes() {
    if (isLocal()) {
        installTokenXAuthMock {
            setAsDefault = false
            alwaysAuthenticated = true
            staticSecurityLevel = SecurityLevel.LEVEL_4
            staticUserPid = "12345678910"
        }
    } else {
        installTokenXAuth()
    }

    configureStatusPages()

    val ctx = AppContext()

    routing {
        route("/api") {
            authenticate(TokenXAuthenticator.name) {
                delbestillingApiAuthenticated(
                    ctx.rolleService,
                    ctx.delbestillingService,
                )
            }

            delbestillingApi(ctx.delbestillingService)
            hjelpemiddelApi(ctx.hjelpemidlerService)

        }

        internal()
    }
}
