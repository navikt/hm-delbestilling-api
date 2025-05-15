package no.nav.hjelpemidler.delbestilling.common

import no.nav.hjelpemidler.delbestilling.config.isDev


enum class Enhet(val nummer: String, private val epost: String) {

    ØST_VIKEN("4701", "nav.hot.ost-viken.teknisk.ordrekontor@nav.no"),
    OSLO("4703", "nav.hot.oslo.teknisk.ordrekontor@nav.no"),
//    INNLANDET_ELVERUM("4704", ""),
//    INNLANDET_GJØVIK("4705", ""),
//    VEST_VIKEN("4706", ""),
//    VESTFOLD_OG_TELEMARK("4707", ""),
//    AGDER("4710", ""),
//    ROGALAND("4711", ""),
//    VESTLAND_BERGEN("4712", ""),
//    VESTLAND_FØRDE("4714", ""),
//    MØRE_OG_ROMSDAL("4715", ""),
//    TRØNDELAG("4716", ""),
//    NORDLAND("4718", ""),
//    TROMS_OG_FINNMARK("4719", ""),
    ;

    fun epost(): String =
        if (isDev()) "digitalisering.av.hjelpemidler.og.tilrettelegging@nav.no" else this.epost


    companion object {
        fun fraEnhetsnummer(enhetNr: String): Enhet {
            return entries.find { it.nummer == enhetNr }
                ?: throw IllegalArgumentException("Enhet $enhetNr ikke funnet.")
        }
    }
}