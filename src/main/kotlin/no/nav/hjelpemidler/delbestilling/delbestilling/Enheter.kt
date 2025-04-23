package no.nav.hjelpemidler.delbestilling.delbestilling
import no.nav.hjelpemidler.delbestilling.config.isDev

val ENHETNR_OSLO = "4703"
val ENHETNR_ØST_VIKEN = "4701"

fun enhetTilEpostadresse(enhetnr: String): String {
    if (isDev()) {
        return "digitalisering.av.hjelpemidler.og.tilrettelegging@nav.no"
    }

    return when (enhetnr) {
        ENHETNR_OSLO -> "nav.hot.oslo.teknisk.ordrekontor@nav.no"
        ENHETNR_ØST_VIKEN -> "nav.hot.ost-viken.teknisk.ordrekontor@nav.no"
        else -> throw IllegalArgumentException("Mangler epostadresse for enhet $enhetnr")
    }
}