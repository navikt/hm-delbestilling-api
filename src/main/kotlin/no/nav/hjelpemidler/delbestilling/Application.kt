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
import no.nav.hjelpemidler.delbestilling.roller.RolleClient
import no.nav.hjelpemidler.delbestilling.roller.RolleService
import no.nav.tms.token.support.tokendings.exchange.TokendingsService
import no.nav.tms.token.support.tokendings.exchange.TokendingsServiceBuilder
import no.nav.tms.token.support.tokenx.validation.TokenXAuthenticator
import no.nav.tms.token.support.tokenx.validation.installTokenXAuth
import no.nav.tms.token.support.tokenx.validation.mock.SecurityLevel
import no.nav.tms.token.support.tokenx.validation.mock.installTokenXAuthMock
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

// TODO: kan dette kanskje gjøres på en bedre måte?
interface TokenService {
    suspend fun exchangeToken(token: String, scope: String): String
}

private class MockedTokendingsService: TokenService {
    override suspend fun exchangeToken(token: String, scope: String): String {
        return token
    }
}

class TokendingsWrapper(val tokendingsService: TokendingsService): TokenService {
    override suspend fun exchangeToken(token: String, scope: String): String {
        return tokendingsService.exchangeToken(token, scope)
    }
}

fun Application.setupRoutes() {
    val delbestillingRepository = DelbestillingRepository(Database.migratedDataSource)
    val tokendingsService = if (isLocal()) MockedTokendingsService() else TokendingsWrapper(TokendingsServiceBuilder.buildTokendingsService())
    val rolleClient = RolleClient(tokendingsService)
    val rolleService = RolleService(rolleClient)

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

    routing {
        route("/api") {
            authenticate(TokenXAuthenticator.name) {
                delbestillingApiAuthenticated(delbestillingRepository, rolleService)
            }

            delbestillingApi()
        }

        internal()
    }
}
