package no.nav.hjelpemidler.delbestilling.oppslag

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Oebs
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Utlån
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.UtlånMedSerienr
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.Pdl
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.PersonNotFoundInPdl


private val log = KotlinLogging.logger {}

class OppslagService(
    private val pdl: Pdl,
    private val oebs: Oebs,
    private val piloterService: PiloterService,
    private val finnDelerTilHjelpemiddel: FinnDelerTilHjelpemiddel,
    private val berikMedLagerstatus: BerikMedLagerstatus,
    private val berikMedDagerSidenForrigeBatteribestilling: BerikMedDagerSidenForrigeBatteribestilling,
) {

    suspend fun slåOppHjelpemiddel(hmsnr: String, serienr: String): OppslagResult = coroutineScope {
        data class BrukerInfo(
            val utlånMedSerienr: UtlånMedSerienr,
            val kommunenummer: String
        )

        val brukerInfoDeferred = async {
            oebs.hentUtlånPåArtNrOgSerienr(hmsnr, serienr)?.let { utlån ->
                log.info { "utlån: $utlån" }
                BrukerInfo(utlån, pdl.hentKommunenummer(utlån.fnr))
            }
        }

        val hjelpemiddelBase = when (val result = finnDelerTilHjelpemiddel(hmsnr, true)) {
            is FinnDelerResultat.Funnet -> result.hjelpemiddel
            is FinnDelerResultat.IkkeFunnet -> return@coroutineScope OppslagResult.Feil(result.feil)
        }

        val brukerInfo = try {
            brukerInfoDeferred.await() ?: return@coroutineScope OppslagResult.Feil(OppslagFeil.INGET_UTLÅN)
        } catch (e: PersonNotFoundInPdl) {
            return@coroutineScope OppslagResult.Feil(OppslagFeil.PERSON_IKKE_FUNNET)
        }

        val hjelpemiddel = hjelpemiddelBase
            .let { berikMedDagerSidenForrigeBatteribestilling(it, serienr) }
            .let { berikMedLagerstatus(it, brukerInfo.kommunenummer) }
            .berikMedGaranti(brukerInfo.utlånMedSerienr)
            .sorterDeler()

        val piloter = piloterService.hentPiloter(brukerInfo.kommunenummer)

        OppslagResult.Suksess(OppslagResultat(hjelpemiddel, piloter))
    }

    suspend fun slåOppHjelpemiddelMedBrukernr(hmsnr: String, brukernr: String): OppslagResult = coroutineScope {
        data class BrukerInfo(
            val utlån: Utlån,
            val kommunenummer: String
        )

        val brukerInfoDeferred = async {
            oebs.hentUtlånPåArtNrOgBrukernr(hmsnr, brukernr)?.let { utlån ->
                log.info { "utlån: $utlån" }
                BrukerInfo(utlån.first(), pdl.hentKommunenummer(utlån.first().fnr))
            }
        }

        val hjelpemiddelBase = when (val result = finnDelerTilHjelpemiddel(hmsnr, true)) {
            is FinnDelerResultat.Funnet -> result.hjelpemiddel
            is FinnDelerResultat.IkkeFunnet -> return@coroutineScope OppslagResult.Feil(result.feil)
        }

        val brukerInfo = try {
            brukerInfoDeferred.await() ?: return@coroutineScope OppslagResult.Feil(OppslagFeil.INGET_UTLÅN)
        } catch (e: PersonNotFoundInPdl) {
            return@coroutineScope OppslagResult.Feil(OppslagFeil.PERSON_IKKE_FUNNET)
        }

        val hjelpemiddel = hjelpemiddelBase
            // .let { berikMedDagerSidenForrigeBatteribestilling(it, serienr) }
            .let { berikMedLagerstatus(it, brukerInfo.kommunenummer) }
            // .berikMedGaranti(brukerInfo.utlån)
            .sorterDeler()

        val piloter = piloterService.hentPiloter(brukerInfo.kommunenummer)

        OppslagResult.Suksess(OppslagResultat(hjelpemiddel, piloter))
    }



}