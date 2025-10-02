package no.nav.hjelpemidler.delbestilling.oppslag

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import no.nav.hjelpemidler.delbestilling.common.Lagerstatus
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Oebs
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.Pdl


private val log = KotlinLogging.logger {}

class OppslagService(
    private val pdl: Pdl,
    private val oebs: Oebs,
    private val piloterService: PiloterService,
    private val finnDelerTilHjelpemiddel: FinnDelerTilHjelpemiddel,
    private val berikMedLagerstatus: BerikMedLagerstatus,
    private val berikMedDagerSidenForrigeBatteribestilling: BerikMedDagerSidenForrigeBatteribestilling
) {

    suspend fun slåOppHjelpemiddel(hmsnr: String, serienr: String): OppslagResultat = coroutineScope {
        val brukersKommunenummerDeferred = async {
            val brukersFnr = oebs.hentFnrLeietaker(hmsnr, serienr)
                ?: throw IngenUtlånException("Fant ingen utlån for hmsnr $hmsnr og serien $serienr")
            pdl.hentKommunenummer(brukersFnr)
        }

        val hjelpemiddel = finnDelerTilHjelpemiddel(hmsnr)
            .let { berikMedDagerSidenForrigeBatteribestilling(it, serienr) }
            .let { berikMedLagerstatus(it, brukersKommunenummerDeferred.await()) }
            .sorterDeler()

        val piloter = piloterService.hentPiloter(brukersKommunenummerDeferred.await())

        OppslagResultat(hjelpemiddel, piloter)
    }
}