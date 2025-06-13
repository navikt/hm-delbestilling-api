package no.nav.hjelpemidler.delbestilling.common

import no.nav.hjelpemidler.delbestilling.config.isDev


enum class Enhet(val nummer: String, private val epost: String?) {

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
    TROMS_OG_FINNMARK("4719", null),
    ;

    fun epost(): String =
        if (isDev()) "digitalisering.av.hjelpemidler.og.tilrettelegging@nav.no" else this.epost
            ?: throw IllegalArgumentException("Enhet $this, mangler epost")


    companion object {
        fun fraEnhetsnummer(enhetNr: String): Enhet {
            val nummerTilEnhet: Map<String, Enhet> = mapOf(
                "4701" to ØST_VIKEN,
                "4702" to OSLO,
                "4703" to OSLO,
                "4704" to INNLANDET_ELVERUM,
                "4705" to INNLANDET_GJØVIK,
                "4706" to VEST_VIKEN,
                "4707" to VESTFOLD_OG_TELEMARK,
                "4708" to VESTFOLD_OG_TELEMARK,
                "4709" to AGDER,
                "4710" to AGDER,
                "4711" to ROGALAND,
                "4712" to VESTLAND_BERGEN,
                "4714" to VESTLAND_FØRDE,
                "4715" to MØRE_OG_ROMSDAL,
                "4716" to TRØNDELAG,
                "4717" to TRØNDELAG,
                "4718" to NORDLAND,
                "4719" to TROMS_OG_FINNMARK,
                "4720" to TROMS_OG_FINNMARK,
            )

            return nummerTilEnhet[enhetNr] ?: throw IllegalArgumentException("Enhet $enhetNr ikke funnet.")
        }
    }
}