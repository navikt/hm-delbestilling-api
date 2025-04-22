package no.nav.hjelpemidler.delbestilling.infrastructure.email

import no.nav.hjelpemidler.delbestilling.config.isDev

fun enhetTilEpostadresse(enhetnr: String): String {
    if (isDev()) {
        return "digitalisering.av.hjelpemidler.og.tilrettelegging@nav.no"
    }

    return when (enhetnr) {
        "4703" -> "nav.hot.oslo.teknisk.ordrekontor@nav.no"
        "4701" -> "nav.hot.ost-viken.teknisk.ordrekontor@nav.no"
        else -> throw IllegalArgumentException("Mangler epostadresse for enhet $enhetnr")
    }
}