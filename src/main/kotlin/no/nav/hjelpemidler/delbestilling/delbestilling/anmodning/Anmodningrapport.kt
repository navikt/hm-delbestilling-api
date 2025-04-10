package no.nav.hjelpemidler.delbestilling.delbestilling.anmodning

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.delbestilling.model.DelLinje
import no.nav.hjelpemidler.delbestilling.delbestilling.model.Lagerstatus
import kotlin.math.abs

private val log = KotlinLogging.logger { }

fun beregnAnmodningsbehovForDel(
    dellinje: DelLinje,
    lagerstatus: Lagerstatus
): AnmodningsbehovForDel {
    return beregnAnmodningsbehovForDel(
        Del(hmsnr = dellinje.del.hmsnr, navn = dellinje.del.navn, antall = dellinje.antall),
        lagerstatus
    )
}

fun beregnAnmodningsbehovForDel(
    del: Del,
    lagerstatus: Lagerstatus
): AnmodningsbehovForDel {
    val (hmsnr, navn, antallBestilt) = del
    log.info { "Vurderer anmodningsbehov for $antallBestilt stk. av $hmsnr med $lagerstatus" }

    if (lagerstatus.minmax) {
        log.info { "$hmsnr er på minmax og trenger derfor ikke anmodes" }
        return AnmodningsbehovForDel(
            hmsnr = hmsnr,
            navn = navn,
            antallBestilt = antallBestilt,
            erPåMinmax = true,
            antallPåLager = lagerstatus.antallDelerPåLager,
            antallSomMåAnmodes = 0,
        )
    }

    val antallPåLager = lagerstatus.antallDelerPåLager
    if (antallPåLager > antallBestilt) {
        log.info { "$hmsnr har nok dekning på lager ($antallPåLager stk.) og trenger derfor ikke anmodes" }
        return AnmodningsbehovForDel(
            hmsnr = hmsnr,
            navn = navn,
            antallBestilt = antallBestilt,
            erPåMinmax = false,
            antallPåLager = lagerstatus.antallDelerPåLager,
            antallSomMåAnmodes = 0,
        )
    }

    val antallTilAnmodning = abs(antallPåLager - antallBestilt)
    log.info { "$hmsnr har ikke dekning på lager ($antallPåLager stk.). Det må anmodes $antallTilAnmodning" }
    return AnmodningsbehovForDel(
        hmsnr = hmsnr,
        navn = navn,
        antallBestilt = antallBestilt,
        erPåMinmax = false,
        antallPåLager = lagerstatus.antallDelerPåLager,
        antallSomMåAnmodes = antallTilAnmodning,
    )
}