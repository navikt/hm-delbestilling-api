package no.nav.hjelpemidler.delbestilling.delbestilling

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.hjelpemidler.database.transaction
import no.nav.hjelpemidler.delbestilling.exceptions.PersonNotAccessibleInPdl
import no.nav.hjelpemidler.delbestilling.exceptions.PersonNotFoundInPdl
import no.nav.hjelpemidler.delbestilling.isProd
import no.nav.hjelpemidler.delbestilling.oebs.Artikkel
import no.nav.hjelpemidler.delbestilling.oebs.OebsService
import no.nav.hjelpemidler.delbestilling.oebs.OpprettBestillingsordreRequest
import no.nav.hjelpemidler.delbestilling.pdl.PdlService
import no.nav.hjelpemidler.delbestilling.roller.RolleService
import no.nav.tms.token.support.tokenx.validation.user.TokenXUserFactory
import javax.sql.DataSource

private val log = KotlinLogging.logger {}

fun Route.delbestillingApi(
    oebsService: OebsService,
) {
    post("/oppslag") {
        try {
            val request = call.receive<OppslagRequest>()
            log.info { "/oppslag request: $request" }

            val hjelpemiddel = hjelpemiddelDeler[request.hmsnr]
                ?: return@post call.respond(OppslagResponse(null, OppslagFeil.TILBYR_IKKE_HJELPEMIDDEL))

            oebsService.hentUtlånPåArtnrOgSerienr(request.hmsnr, request.serienr)
                ?: return@post call.respond(OppslagResponse(null, OppslagFeil.INGET_UTLÅN))

            call.respond(OppslagResponse(hjelpemiddel, null))
        } catch (e: Exception) {
            log.error(e) { "Klarte ikke gjøre oppslag" }
        }
    }
}

fun Route.delbestillingApiAuthenticated(
    delbestillingRepository: DelbestillingRepository,
    rolleService: RolleService,
    delbestillingService: DelbestillingService,
    tokenXUserFactory: TokenXUserFactory = TokenXUserFactory,
) {
    post("/delbestilling") {
        try {
            val request = call.receive<DelbestillingRequest>()
            log.info { "/delbestilling request: $request" }
            val tokenXUser = tokenXUserFactory.createTokenXUser(call)
            val bestillerFnr = tokenXUser.ident

            val delbestillerRolle = rolleService.hentDelbestillerRolle(tokenXUser.tokenString)
            log.info { "delbestillerRolle: $delbestillerRolle" }

            val response = delbestillingService.opprettDelbestilling(delbestillerRolle, request, bestillerFnr)

            if (response.feil == null) {
                call.respond(HttpStatusCode.Created, response)
            } else {
                call.respond(response)
            }

        } catch (e: Exception) {
            log.error(e) { "Innsending av bestilling feilet" }
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

    get("/delbestilling") {
        val bestillerFnr = tokenXUserFactory.createTokenXUser(call).ident
        val delbestillinger = delbestillingRepository.hentDelbestillinger(bestillerFnr)
        call.respond(delbestillinger)
    }
}
