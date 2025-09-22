package no.nav.hjelpemidler.delbestilling.delbestilling.anmodning

import no.nav.hjelpemidler.delbestilling.common.Lager

fun rapportTilMelding(rapport: Anmodningrapport): String {
    val anmodningerPerLeverandør = rapport.anmodningsbehov.groupBy { it.leverandørnavn }

    val leverandørMeldinger = anmodningerPerLeverandør.map { (leverandør, anmodninger) ->
        """
Leverandør: $leverandør
${anmodninger.joinToString("\n") { "${it.hmsnr} (${it.navn}): Må anmodes ${it.antallSomMåAnmodes} stk." }}
            """.trimIndent()
    }

    // Fordi lager Sør-Trøndelag (4716) og Nord-Trøndelag (4717) deler e-post må vi legge til litt ekstra info her.
    val trøndelagLagerInfo = when (rapport.lager.nummer) {
        Lager.SØR_TRØNDELAG.nummer -> {
"OBS: Disse delene skal leveres fra lager Sør-Trøndelag og må anmodes derfra."
        }
        Lager.NORD_TRØNDELAG.nummer -> {
"OBS: Disse delene skal leveres fra lager Nord-Trøndelag og må anmodes derfra."
        }
        else -> ""
    }

    return """
Hei!

Disse delene er bestilt digitalt, men er ikke på lager. Dere må derfor sende anmodning på følgende:

${leverandørMeldinger.joinToString("\n\n") { it }}

${trøndelagLagerInfo}

Dersom dere har spørsmål til dette så kan dere svare oss tilbake på denne e-posten.

Vennlig hilsen
DigiHoT
        """.trimIndent()
}