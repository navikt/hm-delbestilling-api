package no.nav.hjelpemidler.delbestilling.delbestilling

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.hjelpemidler.delbestilling.exceptions.PersonNotAccessibleInPdl
import no.nav.hjelpemidler.delbestilling.exceptions.PersonNotFoundInPdl
import no.nav.hjelpemidler.delbestilling.isProd
import no.nav.hjelpemidler.delbestilling.oebs.Artikkel
import no.nav.hjelpemidler.delbestilling.oebs.OebsService
import no.nav.hjelpemidler.delbestilling.oebs.OpprettBestillingsordreRequest
import no.nav.hjelpemidler.delbestilling.pdl.PdlService
import no.nav.hjelpemidler.delbestilling.roller.RolleService
import no.nav.tms.token.support.tokenx.validation.user.TokenXUserFactory

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
    pdlService: PdlService,
    oebsService: OebsService,
    tokenXUserFactory: TokenXUserFactory = TokenXUserFactory,
) {
    post("/delbestilling") {
        try {
            val request = call.receive<DelbestillingRequest>()
            log.info { "/delbestilling request: $request" }
            val tokenXUser = tokenXUserFactory.createTokenXUser(call)
            val bestillerFnr = tokenXUser.ident

            val id = request.delbestilling.id
            val hmsnr = request.delbestilling.hmsnr.value
            val serienr = request.delbestilling.serienr.value

            val delbestillerRolle = rolleService.hentDelbestillerRolle(tokenXUser.tokenString)
            log.info { "delbestillerRolle: $delbestillerRolle" }
            if (!delbestillerRolle.kanBestilleDeler) {
                call.respond(HttpStatusCode.Forbidden, "Du har ikke rettighet til å gjøre dette")
            }

            val utlån = oebsService.hentUtlånPåArtnrOgSerienr(hmsnr, serienr)
                ?: return@post call.respond(DelbestillingResponse(id, feil = DelbestillingFeil.INGET_UTLÅN))

            // val brukerFnr = "03441558383" // Test av adressebeskyttelse
            // val brukerFnr = "11111111111" // Test av person ikke funnet
            val brukerFnr = utlån.fnr

            // TODO: det føles litt feil å gjøre alle disse sjekkene her
            val brukerKommunenr = try {
                pdlService.hentKommunenummer(brukerFnr)
            } catch (e: PersonNotAccessibleInPdl) {
                log.error(e) { "Person ikke tilgjengelig i PDL" }
                return@post call.respond(DelbestillingResponse(id, feil = DelbestillingFeil.BRUKER_IKKE_FUNNET))
            } catch(e: PersonNotFoundInPdl) {
                log.error(e) { "Person ikke funnet i PDL" }
                return@post call.respond(DelbestillingResponse(id, feil = DelbestillingFeil.BRUKER_IKKE_FUNNET))
            } catch (e: Exception) {
                log.error(e) { "Klarte ikke å hente bruker fra PDL" }
                throw e
            }

            // Sjekk om en av innsenders kommuner tilhører brukers kommuner
            val innsenderRepresentererBrukersKommune =
                delbestillerRolle.kommunaleOrgs?.find { it.kommunenummer == brukerKommunenr } != null

            // Skrur av denne sjekken for dev akkurat nå, da det er litt mismatch i testdataen der
            if (isProd() && !innsenderRepresentererBrukersKommune) {
                return@post call.respond(DelbestillingResponse(id, feil = DelbestillingFeil.ULIK_GEOGRAFISK_TILKNYTNING))
            }

            // TODO transaction {
            delbestillingRepository.lagreDelbestilling(bestillerFnr, brukerFnr, brukerKommunenr, request.delbestilling)
            val bestillersNavn = pdlService.hentPersonNavn(bestillerFnr)
            val deler = request.delbestilling.deler.map { Artikkel(it.hmsnr, it.antall) }
            oebsService.sendDelbestilling(
                OpprettBestillingsordreRequest(
                    brukersFnr = brukerFnr,
                    saksnummer = id.toString(),
                    innsendernavn = bestillersNavn,
                    artikler = deler
                )
            )

            log.info { "Delbestilling '$id' sendt inn" }

            call.respond(HttpStatusCode.Created, DelbestillingResponse(id, null))
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
