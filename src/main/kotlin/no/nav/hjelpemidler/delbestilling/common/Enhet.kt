package no.nav.hjelpemidler.delbestilling.common

import no.nav.hjelpemidler.delbestilling.config.isDev


enum class Enhet(val nummer: String, private val epost: String) {

    ØST_VIKEN("4701", "nav.hot.ost-viken.teknisk.ordrekontor@nav.no"),
    OSLO("4703", "nav.hot.oslo.teknisk.ordrekontor@nav.no");

    fun epost(): String =
        if (isDev()) "digitalisering.av.hjelpemidler.og.tilrettelegging@nav.no" else this.epost


    companion object {
        fun fraEnhetsnummer(enhetNr: String): Enhet {
            return entries.find { it.nummer == enhetNr }
                ?: throw IllegalArgumentException("Enhet $enhetNr ikke funnet.")
        }
    }
}