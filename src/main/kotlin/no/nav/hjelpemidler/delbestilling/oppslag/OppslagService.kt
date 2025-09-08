package no.nav.hjelpemidler.delbestilling.oppslag

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import no.nav.hjelpemidler.delbestilling.common.Lagerstatus
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

        val hjelpemiddel = finnDelerTilHjelpemiddel(hmsnr)
            .let { berikMedDagerSidenForrigeBatteribestilling(it, serienr) }
            .let { berikMedLagerstatus(it, brukerInfoDeferred.await().kommunenummer) }
            .let { berikMedGaranti(it, brukerInfoDeferred.await().utlån.opprettetDato ) }
            .sorterDeler()

        val piloter = piloterService.hentPiloter(brukerInfoDeferred.await().kommunenummer)

        OppslagResultat(hjelpemiddel, piloter)
    }

    suspend fun EKSTERN_DEV_slåOppHjelpemiddel(hmsnr: String, serienr: String): OppslagResultat {
        log.info { "Slår opp hmsnr=$hmsnr for dev.ekstern, og beriker med fake lagerstatus" }
        var hjelpemiddel = finnDelerTilHjelpemiddel(hmsnr).sorterDeler()

        // legg på pseudo-random lagerstatus
        val delerMedLagerstatus = hjelpemiddel.deler.map { del ->
            val erMinmax = del.hmsnr.toInt() % 3 != 0                // Gjør ca 66% tilgjengelig
            val antallPåLager = del.hmsnr.takeLast(1).toInt()    // Antall tilgjengelig = siste siffer i hmsnr
            del.copy(
                lagerstatus = Lagerstatus(
                    organisasjons_id = 292,
                    organisasjons_navn = "*19 Troms",
                    artikkelnummer = del.hmsnr,
                    minmax = erMinmax,
                    tilgjengelig = antallPåLager,
                    antallDelerPåLager = antallPåLager
                )
            )
        }

        if (hjelpemiddel.harBatteri()) {
            hjelpemiddel = hjelpemiddel.copy(antallDagerSidenSistBatteribestilling = serienr.take(3).toInt())
        }

        return OppslagResultat(hjelpemiddel.copy(deler = delerMedLagerstatus))
    }
}