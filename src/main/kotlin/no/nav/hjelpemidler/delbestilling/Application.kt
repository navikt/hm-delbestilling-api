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
import no.nav.hjelpemidler.delbestilling.oebs.OebsApiProxyClient
import no.nav.hjelpemidler.delbestilling.oebs.OebsProxyApiService
import no.nav.hjelpemidler.delbestilling.roller.RolleClient
import no.nav.hjelpemidler.delbestilling.roller.RolleService
import no.nav.tms.token.support.tokendings.exchange.TokendingsService
import no.nav.tms.token.support.tokendings.exchange.TokendingsServiceBuilder
import no.nav.tms.token.support.tokenx.validation.TokenXAuthenticator
import no.nav.tms.token.support.tokenx.validation.installTokenXAuth
import no.nav.tms.token.support.tokenx.validation.mock.SecurityLevel
import no.nav.tms.token.support.tokenx.validation.mock.installTokenXAuthMock
import java.util.TimeZone
import no.nav.hjelpemidler.delbestilling.pdl.PdlClient
import no.nav.hjelpemidler.http.openid.azureADClient
import no.nav.tms.token.support.azure.exchange.AzureServiceBuilder
import kotlin.time.Duration.Companion.seconds

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

    val azureAd = AzureServiceBuilder.buildAzureService(
        cachingEnabled = true,
        maxCachedEntries = 100,
        cacheExpiryMarginSeconds = 10,
        enableDefaultProxy = true
    )

    val azureClient = azureADClient {
        cache(leeway = 10.seconds) {
            maximumSize = 100
        }
    }

    val pdlClient = PdlClient(azureAd)

    val oebsApiProxyClient = OebsApiProxyClient(azureClient)
    val oebsService = OebsProxyApiService(oebsApiProxyClient)

    routing {
        route("/api") {
            authenticate(TokenXAuthenticator.name) {
                delbestillingApiAuthenticated(delbestillingRepository, pdlClient = pdlClient, rolleService = rolleService)
            }

            delbestillingApi(oebsService)
        }

        internal()
    }
}
