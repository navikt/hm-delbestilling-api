package no.nav.hjelpemidler.delbestilling.common

import no.nav.hjelpemidler.delbestilling.config.isDev

enum class Lager(val nummer: String, private val epost: String?) {

    ØST_VIKEN("4701", "nav.hot.ost-viken.teknisk.ordrekontor@nav.no"),
    OSLO("4703", "nav.hot.oslo.teknisk.ordrekontor@nav.no"),
    INNLANDET_ELVERUM("4704", "nav.hot.innlandet.elverum.teknisk@nav.no"),
    INNLANDET_GJØVIK("4705", "nav.hot.innlandet.gjovik.teknisk.verksted@nav.no"),
    VEST_VIKEN("4706", "nav.hot.vest-viken.lager@nav.no"),
    VESTFOLD_OG_TELEMARK("4707", "nav.hot.vestfold.og.telemark.teknisk@nav.no"),
    AGDER("4710", "nav.hot.agder.teknisk@nav.no"),
    ROGALAND("4711", "nav.hot.rogaland.teknisk@nav.no"),
    VESTLAND_BERGEN("4712", "nav.hot.vestland.bergen.delelager@nav.no"),
    VESTLAND_FØRDE("4714", "nav.hot.vestland.forde@nav.no"),
    MØRE_OG_ROMSDAL("4715", "nav.hot.moreogromsdal.teknisk@nav.no"),
    TRØNDELAG("4716", "nav.hot.trondelag.teknisk@nav.no"),
    NORDLAND("4718", "nav.hot.nordland.teknisk@nav.no"),
    TROMS("4719", "nav.hot.troms.og.finnmark.tromso.teknisk@nav.no"),
    FINNMARK("4720", "nav.hot.troms.og.finnmark.lakselv.teknisk@nav.no"),
    ;

    fun epost(): String =
        if (isDev()) "digitalisering.av.hjelpemidler.og.tilrettelegging@nav.no" else this.epost
            ?: throw IllegalArgumentException("Lager $this, mangler epost")


    companion object {
        fun fraLagernummer(lagernummer: String): Lager {
            return entries.find { it.nummer == lagernummer }
                ?: throw IllegalArgumentException("Lager for lagernummer $lagernummer ikke funnet.")
        }
    }
}