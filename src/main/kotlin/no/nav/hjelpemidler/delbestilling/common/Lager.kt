package no.nav.hjelpemidler.delbestilling.common

import no.nav.hjelpemidler.delbestilling.config.isDev

enum class Lager(val nummer: String, val navn: String, private val epost: String?) {

    ØST_VIKEN("4701", "Øst-Viken", "nav.hot.ost-viken.teknisk.ordrekontor@nav.no"),
    OSLO("4703", "Oslo", "nav.hot.oslo.teknisk.ordrekontor@nav.no"),
    INNLANDET_ELVERUM("4704", "Elverum", "nav.hot.innlandet.elverum.teknisk@nav.no"),
    INNLANDET_GJØVIK("4705", "Gjøvik", "nav.hot.innlandet.gjovik.teknisk.verksted@nav.no"),
    VEST_VIKEN("4706", "Vest-Viken", "nav.hot.vest-viken.lager@nav.no"),
    VESTFOLD_OG_TELEMARK("4707", "Vestfold og Telemark", "nav.hot.vestfold.og.telemark.teknisk@nav.no"),
    AGDER("4710", "Agder", "nav.hot.agder.teknisk@nav.no"),
    ROGALAND("4711", "Rogaland", "nav.hot.rogaland.teknisk@nav.no"),
    VESTLAND_BERGEN("4712", "Bergen", "nav.hot.vestland.bergen.delelager@nav.no"),
    VESTLAND_FØRDE("4714", "Førde", "nav.hot.vestland.forde@nav.no"),
    MØRE_OG_ROMSDAL("4715", "Møre og Romsdal", "nav.hot.moreogromsdal.teknisk@nav.no"),
    SØR_TRØNDELAG("4716", "Sør-Trøndelag", "nav.hot.trondelag.teknisk@nav.no"),
    NORD_TRØNDELAG("4717", "Nord-Trøndelag", "nav.hot.trondelag.teknisk@nav.no"),
    NORDLAND("4718", "Nordland", "nav.hot.nordland.teknisk@nav.no"),
    TROMS("4719", "Troms", "nav.hot.troms.og.finnmark.tromso.teknisk@nav.no"),
    FINNMARK("4720", "Finnmark", "nav.hot.troms.og.finnmark.lakselv.teknisk@nav.no"),
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