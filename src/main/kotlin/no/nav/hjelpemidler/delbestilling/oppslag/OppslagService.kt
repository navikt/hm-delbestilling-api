package no.nav.hjelpemidler.delbestilling.oppslag

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import no.nav.hjelpemidler.delbestilling.common.Lagerstatus
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingRepository
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Oebs
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.Pdl
import java.time.LocalDate
import java.time.temporal.ChronoUnit


private val log = KotlinLogging.logger {}

class OppslagService(
    private val pdl: Pdl,
    private val oebs: Oebs,
    private val piloterService: PiloterService,
    private val finnDelerTilHjelpemiddel: FinnDelerTilHjelpemiddel,
    private val berikMedLagerstatus: BerikMedLagerstatus,
    private val delbestillingRepository: DelbestillingRepository,
) {

    suspend fun slåOppHjelpemiddel(hmsnr: String, serienr: String): OppslagResultat = coroutineScope {
        val brukersKommunenummerResult = async {
            val brukersFnr = oebs.hentFnrLeietaker(hmsnr, serienr)
                ?: throw IngenUtlånException("Fant ingen utlån for hmsnr $hmsnr og serien $serienr")
            pdl.hentKommunenummer(brukersFnr)
        }

        var hjelpemiddel = finnDelerTilHjelpemiddel.execute(hmsnr)

        if (hjelpemiddel.deler.any { it.kategori == "Batteri" }) {
            hjelpemiddel = hjelpemiddel.copy(antallDagerSidenSistBatteribestilling = antallDagerSidenSisteBatteribestilling(hmsnr, serienr))
        }

        val brukersKommunenummer = brukersKommunenummerResult.await()
        hjelpemiddel = berikMedLagerstatus.execute(hjelpemiddel, brukersKommunenummer)
        val piloter = piloterService.hentPiloter(brukersKommunenummer)

        OppslagResultat(hjelpemiddel, piloter)
    }

    suspend fun EKSTERN_DEV_slåOppHjelpemiddel(hmsnr: String): OppslagResultat {
        log.info { "Slår opp hmsnr=$hmsnr for dev.ekstern, og beriker med fake lagerstatus" }
        val hjelpemiddel = finnDelerTilHjelpemiddel.execute(hmsnr).sorterDeler()

        // legg på pseudo-random lagerstatus
        val delerMedLagerstatus = hjelpemiddel.deler.map { del ->
            val erMinmax = del.hmsnr.toInt() % 3 != 0                // Gjør ca 66% tilgjengelig
            val antallPåLager = del.hmsnr.takeLast(1).toInt()    // Antall tilgjengelig = siste siffer i hmsnr
            del.copy(lagerstatus = Lagerstatus(
                organisasjons_id = 292,
                organisasjons_navn = "*19 Troms",
                artikkelnummer = del.hmsnr,
                minmax = erMinmax,
                tilgjengelig = antallPåLager,
                antallDelerPåLager = antallPåLager
            )
            )
        }

        return OppslagResultat(hjelpemiddel.copy(deler = delerMedLagerstatus))
    }

    fun antallDagerSidenSisteBatteribestilling(hmsnr: String, serienr: String): Int? {
        val dellbestillinger = delbestillingRepository.hentDelbestillinger(hmsnr, serienr)

        val sisteBatteribestilling = dellbestillinger.filter { bestilling ->
            bestilling.delbestilling.deler.any { dellinje ->
                dellinje.del.kategori == "Batteri"
            }
        }.maxByOrNull { it.opprettet } ?: return null

        val antallDagerSiden = sisteBatteribestilling.opprettet.toLocalDate()
            .until(LocalDate.now(), ChronoUnit.DAYS).toInt()

        return antallDagerSiden
    }
}