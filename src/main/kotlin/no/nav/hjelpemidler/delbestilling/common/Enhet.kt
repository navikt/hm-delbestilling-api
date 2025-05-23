package no.nav.hjelpemidler.delbestilling.common

import no.nav.hjelpemidler.delbestilling.config.isDev


enum class Enhet(val nummer: String, private val epost: String?) {

    ØST_VIKEN("4701", "nav.hot.ost-viken.teknisk.ordrekontor@nav.no"),
    OSLO("4703", "nav.hot.oslo.teknisk.ordrekontor@nav.no"),
    INNLANDET_ELVERUM("4704", "nav.hot.innlandet.elverum.teknisk@nav.no"),
    INNLANDET_GJØVIK("4705", null),
    VEST_VIKEN("4706", null),
    VESTFOLD_OG_TELEMARK("4707", "nav.hot.vestfold.og.telemark.teknisk@nav.no"),
    AGDER("4710", null),
    ROGALAND("4711", null),
    VESTLAND_BERGEN("4712", null),
    VESTLAND_FØRDE("4714", null),
    MØRE_OG_ROMSDAL("4715", null),
    TRØNDELAG("4716", null),
    NORDLAND("4718", null),
    TROMS_OG_FINNMARK("4719", null),
    ;

    fun epost(): String =
        if (isDev()) "digitalisering.av.hjelpemidler.og.tilrettelegging@nav.no" else this.epost
            ?: throw IllegalArgumentException("Enhet $this, mangler epost")


    companion object {
        fun fraEnhetsnummer(enhetNr: String): Enhet {
            return entries.find { it.nummer == enhetNr }
                ?: throw IllegalArgumentException("Enhet $enhetNr ikke funnet.")
        }
    }
}