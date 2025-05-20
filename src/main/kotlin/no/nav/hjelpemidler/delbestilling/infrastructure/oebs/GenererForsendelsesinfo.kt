package no.nav.hjelpemidler.delbestilling.infrastructure.oebs

import no.nav.hjelpemidler.delbestilling.common.Levering

fun genererForsendelsesinfo(levering: Levering, navn: String): String {
    val xkLagerInfo = if (levering == Levering.TIL_XK_LAGER) "XK-Lager " else ""
    return "${xkLagerInfo}Del bestilt av: $navn"
}