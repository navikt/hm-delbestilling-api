package no.nav.hjelpemidler.delbestilling.delbestilling.anmodning

import no.nav.hjelpemidler.delbestilling.common.Enhet
import no.nav.hjelpemidler.delbestilling.delbestilling.model.Hmsnr

data class Del(
    val hmsnr: Hmsnr,
    val navn: String,
    val antall: Int,
)

data class Anmodningrapport(
    val enhet: Enhet,
    val anmodningsbehov: List<AnmodningsbehovForDel>
)

data class AnmodningsbehovForDel(
    val hmsnr: Hmsnr,
    val navn: String,
    val antallBestilt: Int,
    val erPåMinmax: Boolean,
    val antallPåLager: Int,
    val antallSomMåAnmodes: Int,
    var leverandørnavn: String = "IKKE_SATT",
)