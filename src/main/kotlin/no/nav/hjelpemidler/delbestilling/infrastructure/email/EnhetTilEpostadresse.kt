package no.nav.hjelpemidler.delbestilling.infrastructure.email

import no.nav.hjelpemidler.delbestilling.isDev

fun enhetTilEpostadresse(enhetnr: String): String {
    if (isDev()) {
        return "digitalisering.av.hjelpemidler.og.tilrettelegging@nav.no"
    }

    return when (enhetnr) {
        "4703" -> throw IllegalArgumentException("Mangler epostadresse for Oslo enhet $enhetnr")
        else -> throw IllegalArgumentException("Mangler epostadresse for enhet $enhetnr")
    }
}