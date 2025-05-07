package no.nav.hjelpemidler.delbestilling.oppslag

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import no.nav.hjelpemidler.delbestilling.delbestilling.PiloterService
import no.nav.hjelpemidler.delbestilling.delbestilling.model.Lagerstatus
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Oebs
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.Pdl


private val log = KotlinLogging.logger {}

class OppslagService(
    private val pdl: Pdl,
    private val oebs: Oebs,
    private val piloterService: PiloterService,
    private val finnDelerTilHjelpemiddel: FinnDelerTilHjelpemiddel,
    private val berikMedLagerstatus: BerikMedLagerstatus,
) {

    suspend fun slåOppHjelpemiddel(hmsnr: String, serienr: String): OppslagResultat = coroutineScope {
        val brukersKommunenummerResult = async {
            val brukersFnr = oebs.hentFnrLeietaker(hmsnr, serienr)
                ?: throw IngenUtlånException("Fant ingen utlån for hmsnr $hmsnr og serien $serienr")
            pdl.hentKommunenummer(brukersFnr)
        }

        val hjelpemiddel = finnDelerTilHjelpemiddel.execute(hmsnr)
        val brukersKommunenummer = brukersKommunenummerResult.await()

        berikMedLagerstatus.execute(hjelpemiddel, brukersKommunenummer)
        val piloter = piloterService.hentPiloter(brukersKommunenummer)

        OppslagResultat(hjelpemiddel, piloter)
    }

    suspend fun EKSTERN_DEV_slåOppHjelpemiddel(hmsnr: String): OppslagResultat {
        log.info { "Slår opp hmsnr=$hmsnr for dev.ekstern, og beriker med fake lagerstatus" }
        val hjelpemiddel = finnDelerTilHjelpemiddel.execute(hmsnr)

        // Sorter på navn
        val deler = hjelpemiddel.deler.sortedBy { it.navn }

        // legg på pseudo-random lagerstatus
        hjelpemiddel.deler.forEach {
            val erMinmax = it.hmsnr.toInt() % 3 != 0                // Gjør ca 66% tilgjengelig
            val antallPåLager = it.hmsnr.takeLast(1).toInt()    // Antall tilgjengelig = siste siffer i hmsnr
            it.lagerstatus = Lagerstatus(
                organisasjons_id = 292,
                organisasjons_navn = "*19 Troms",
                artikkelnummer = it.hmsnr,
                minmax = erMinmax,
                tilgjengelig = antallPåLager,
                antallDelerPåLager = antallPåLager
            )
        }

        return OppslagResultat(hjelpemiddel.copy(deler = deler))
    }
}