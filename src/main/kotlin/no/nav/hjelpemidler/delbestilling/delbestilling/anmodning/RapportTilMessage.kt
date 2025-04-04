package no.nav.hjelpemidler.delbestilling.delbestilling.anmodning

fun rapportTilMelding(rapport: Anmodningrapport): String {
    val anmodningerPerLeverandør = rapport.anmodningsbehov.groupBy { it.leverandørnavn }

    val leverandørMeldinger = anmodningerPerLeverandør.map { (leverandør, anmodninger) ->
        """
Leverandør: $leverandør
${anmodninger.joinToString("\n") { "${it.hmsnr} (${it.navn}): Må anmodes ${it.antallSomMåAnmodes} stk." }}
            """.trimIndent()
    }

    return """
Hei!

Følgende deler har nylig blitt bestilt digitalt, uten lagerdekning, og må derfor anmodes manuelt.

${leverandørMeldinger.joinToString("\n\n") { it }}

Dersom dere har spørsmål til dette så kan dere svare oss tilbake på denne e-posten.

Vennlig hilsen
DigiHoT
        """.trimIndent()
}