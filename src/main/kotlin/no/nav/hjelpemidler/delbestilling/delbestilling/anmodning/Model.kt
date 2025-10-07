package no.nav.hjelpemidler.delbestilling.delbestilling.anmodning

import no.nav.hjelpemidler.delbestilling.common.Lager
import no.nav.hjelpemidler.delbestilling.common.Hmsnr

data class Del(
    val hmsnr: Hmsnr,
    val navn: String,
    val antall: Int,
)

enum class DelerTilAnmodningStatus {
    /**
     * Venter på behandling ved nattlig jobb.
     */
    AVVENTER,

    /**
     * Raden er behandlet. Delbehovet har enten blitt dekket av etterfylling i løpet av dagen,
     * eller så har det blitt sendt ut mail om anmodningsbehov.
     */
    BEHANDLET,

    /**
     * Den tilhørene ordren/saken ble annulert før raden ble behandlet. Raden er dermed ikke lenger relevant for vurdering av anmodningsbehov.
     */
    ANNULERT,
}

data class Anmodningrapport(
    val lager: Lager,
    val anmodningsbehov: List<AnmodningsbehovForDel>,
    val delerSomIkkeLengerMåAnmodes: List<Del>,
)

data class AnmodningsbehovForDel(
    val hmsnr: Hmsnr,
    val navn: String,
    val antallBestilt: Int,
    val erPåMinmax: Boolean,
    val antallPåLager: Int,
    val antallSomMåAnmodes: Int,
    var leverandørnavn: String = "IKKE_SATT",
) {
    fun tilDel(): Del = Del(
        hmsnr = hmsnr,
        navn = navn,
        antall = antallBestilt,
    )
}