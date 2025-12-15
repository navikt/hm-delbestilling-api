package no.nav.hjelpemidler.delbestilling.oppslag

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Oebs
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Utlån
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.Pdl


private val log = KotlinLogging.logger {}

class OppslagService(
    private val pdl: Pdl,
    private val oebs: Oebs,
    private val piloterService: PiloterService,
    private val finnDelerTilHjelpemiddel: FinnDelerTilHjelpemiddel,
    private val berikMedLagerstatus: BerikMedLagerstatus,
    private val berikMedDagerSidenForrigeBatteribestilling: BerikMedDagerSidenForrigeBatteribestilling,
) {

    suspend fun slåOppHjelpemiddel(hmsnr: String, serienr: String): OppslagResultat = coroutineScope {
        data class BrukerInfo(
            val utlån: Utlån,
            val kommunenummer: String
        )

        val brukerInfoDeferred = async {
            val utlån = oebs.hentUtlånPåArtNrOgSerienr(hmsnr, serienr)
                ?: throw IngenUtlånException("Fant ingen utlån for hmsnr $hmsnr og serien $serienr")
            log.info { "utlån: $utlån" }
            val kommunenummer = pdl.hentKommunenummer(utlån.fnr)
            BrukerInfo(utlån, kommunenummer)
        }

        val hjelpemiddel = finnDelerTilHjelpemiddel(hmsnr, true)
            .let { berikMedDagerSidenForrigeBatteribestilling(it, serienr) }
            .let { berikMedLagerstatus(it, brukerInfoDeferred.await().kommunenummer) }
            .berikMedGaranti(brukerInfoDeferred.await().utlån)
            .sorterDeler()

        val piloter = piloterService.hentPiloter()

        OppslagResultat(hjelpemiddel, piloter)
    }

}