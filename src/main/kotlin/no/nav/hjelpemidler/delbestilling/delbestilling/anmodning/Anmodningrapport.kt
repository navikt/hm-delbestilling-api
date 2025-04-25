package no.nav.hjelpemidler.delbestilling.delbestilling.anmodning

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.delbestilling.model.Lagerstatus
import org.apache.commons.lang3.math.NumberUtils.min
import kotlin.math.abs

private val log = KotlinLogging.logger { }

fun beregnAnmodningsbehovForDelVedInnsending(
    del: Del,
    lagerstatus: Lagerstatus
): AnmodningsbehovForDel {
    val (hmsnr, navn, antallBestilt) = del
    log.info { "Vurderer anmodningsbehov ved innsending for $antallBestilt stk. av $hmsnr med $lagerstatus" }

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
        log.info { "$hmsnr har nok dekning på lager (antallPåLager: $antallPåLager, antallBestilt: $antallBestilt) og trenger derfor ikke anmodes" }
        return AnmodningsbehovForDel(
            hmsnr = hmsnr,
            navn = navn,
            antallBestilt = antallBestilt,
            erPåMinmax = false,
            antallPåLager = lagerstatus.antallDelerPåLager,
            antallSomMåAnmodes = 0,
        )
    }

    if (antallPåLager > 0) {
        val antallTilAnmodning =  antallBestilt - antallPåLager
        log.info { "$hmsnr har ikke dekning på lager (antallPåLager: $antallPåLager, antallBestilt: $antallBestilt). Det må anmodes $antallTilAnmodning" }
        return AnmodningsbehovForDel(
            hmsnr = hmsnr,
            navn = navn,
            antallBestilt = antallBestilt,
            erPåMinmax = false,
            antallPåLager = lagerstatus.antallDelerPåLager,
            antallSomMåAnmodes = antallTilAnmodning,
        )
    }

    val antallTilAnmodning = antallBestilt
    log.info { "$hmsnr har ikke dekning på lager (antallPåLager: $antallPåLager, antallBestilt: $antallBestilt). Det må anmodes $antallTilAnmodning" }
    return AnmodningsbehovForDel(
        hmsnr = hmsnr,
        navn = navn,
        antallBestilt = antallBestilt,
        erPåMinmax = false,
        antallPåLager = lagerstatus.antallDelerPåLager,
        antallSomMåAnmodes = antallTilAnmodning,
    )
}

fun beregnAnmodningsbehovVedRapportering(
    del: Del,
    lagerstatus: Lagerstatus
): AnmodningsbehovForDel {
    val (hmsnr, navn, antallBestilt) = del
    log.info { "Vurderer anmodningsbehov ved rapportering for $antallBestilt stk. av $hmsnr med $lagerstatus" }

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

    // OBS: Her vil antallPåLager være medregnet det som er bestilt i løpet av dagen.
    val antallPåLager = lagerstatus.antallDelerPåLager
    if (antallPåLager >= 0) {
        log.info { "$hmsnr har nok dekning på lager (antallPåLager: $antallPåLager, antallBestilt: $antallBestilt) og trenger derfor ikke anmodes" }
        return AnmodningsbehovForDel(
            hmsnr = hmsnr,
            navn = navn,
            antallBestilt = antallBestilt,
            erPåMinmax = false,
            antallPåLager = lagerstatus.antallDelerPåLager,
            antallSomMåAnmodes = 0,
        )
    }

    val antallTilAnmodning = min(antallBestilt, abs(antallPåLager))
    log.info { "$hmsnr har ikke dekning på lager (antallPåLager: $antallPåLager, antallBestilt: $antallBestilt). Det må anmodes $antallTilAnmodning" }
    return AnmodningsbehovForDel(
        hmsnr = hmsnr,
        navn = navn,
        antallBestilt = antallBestilt,
        erPåMinmax = false,
        antallPåLager = lagerstatus.antallDelerPåLager,
        antallSomMåAnmodes = antallTilAnmodning,
    )
}