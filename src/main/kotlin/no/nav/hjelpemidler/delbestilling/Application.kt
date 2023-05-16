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
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingRepository
import no.nav.hjelpemidler.delbestilling.delbestilling.delbestillingApi
import no.nav.hjelpemidler.delbestilling.delbestilling.delbestillingApiAuthenticated
import no.nav.tms.token.support.tokenx.validation.TokenXAuthenticator
import no.nav.tms.token.support.tokenx.validation.installTokenXAuth
import no.nav.tms.token.support.tokenx.validation.mock.SecurityLevel
import no.nav.tms.token.support.tokenx.validation.mock.installTokenXAuthMock
import java.util.TimeZone
import no.nav.hjelpemidler.delbestilling.pdl.PdlClient
import no.nav.tms.token.support.azure.exchange.AzureServiceBuilder

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

    val delbestillingRepository = DelbestillingRepository(Database.migratedDataSource)

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

    val azureAd = AzureServiceBuilder.buildAzureService(
        cachingEnabled = true,
        maxCachedEntries = 100,
        cacheExpiryMarginSeconds = 10,
        enableDefaultProxy = true
    )

    val pdlClient = PdlClient(azureAd)

    routing {

        route("/api") {
            authenticate(TokenXAuthenticator.name) {
                delbestillingApiAuthenticated(delbestillingRepository, pdlClient = pdlClient)
            }

            delbestillingApi()
        }

        internal()

    }
}
