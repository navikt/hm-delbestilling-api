package no.nav.hjelpemidler.delbestilling.delbestilling

import no.nav.hjelpemidler.delbestilling.common.Delbestilling
import no.nav.hjelpemidler.delbestilling.common.Levering

const val MANUELL_DELBESTILLING_EPOST_EMNE = "Delbestilling til manuell behandling"

data class ManuellDelbestillingEpost(
    val delbestilling: Delbestilling,
    val saksnummer: Long,
    val bestillerNavn: String? = null,
) {
    fun tilHtml(): String {
        val hjelpemiddelIdentifikatorer = listOfNotNull(
            nøkkelverdi("HMS-nr.", delbestilling.hmsnr),
            delbestilling.serienr?.let { nøkkelverdi("Serienr.", it) },
            delbestilling.brukernr?.let { nøkkelverdi("Brukernr.", it) },
        ).joinToString("")

        val deler = delbestilling.deler.joinToString("") { dellinje ->
            delrad(
                identifikator = "HMS-nr. ${dellinje.del.hmsnr}",
                navn = dellinje.del.navn,
                beskrivelse = null,
                antall = dellinje.antall,
            )
        } + delbestilling.ukjenteDeler.joinToString("") { dellinje ->
            val del = dellinje.delUkjent
            delrad(
                identifikator = del.hmsnr?.let { "HMS-nr. $it" } ?: "Lev.art.nr. ${del.levArtNr.orEmpty()}",
                navn = null,
                beskrivelse = del.beskrivelse,
                antall = dellinje.antall,
            )
        }

        val levering = when (delbestilling.levering) {
            Levering.TIL_XK_LAGER -> "Til XK-lager"
            Levering.TIL_SERVICE_OPPDRAG -> "Brukes i serviceoppdrag"
        }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>$MANUELL_DELBESTILLING_EPOST_EMNE</title>
            </head>
            <body style="font-family: Arial, sans-serif; color: #262626; line-height: 1.5;">
                <h1 style="font-size: 24px; margin: 0 0 16px;">Bestilling av deler til manuell behandling</h1>

                <p><strong>Innsender har sendt inn bestillingen manuelt. Kontroller opplysningene og avklar eventuelle uklarheter før du oppretter ordren.</strong></p>
                <h2 style="font-size: 18px; margin: 24px 0 8px;">Kontroller før du oppretter ordren</h2>
                <ul style="padding-left: 24px; max-width: 680px;">
                    <li>Sjekk at artikkelnummeret er riktig.</li>
                    <li>Avklar om det som bestilles, er en del eller tilbehør.</li>
                    <li>Hvis det er tilbehør, sjekk om det finnes et vedtak på tilbehøret. Hvis det ikke finnes et vedtak, må det søkes om tilbehøret.</li>
                    <li>Sjekk at delen eller tilbehøret passer til hjelpemiddelet.</li>
                    <li>Hvis det bestilles batteri, sjekk om hjelpemiddelet er innenfor garantitiden, eller om batteriet er byttet de siste 12 månedene.</li>
                </ul>
                <p>Når du har gjennomført kontrollene og avklart eventuelle uklarheter med innsender, må du opprette ordren manuelt i OeBS.</p>

                <h2 style="font-size: 18px; margin: 24px 0 8px;">Hjelpemiddelet som skal repareres</h2>
                <table role="presentation" style="border-collapse: collapse; width: 100%; max-width: 680px;">
                    <tbody>
                        ${nøkkelverdi("Hjelpemiddel", delbestilling.navn)}
                        $hjelpemiddelIdentifikatorer
                    </tbody>
                </table>

                <h2 style="font-size: 18px; margin: 24px 0 8px;">Deler til reparasjonen</h2>
                <table style="border-collapse: collapse; width: 100%; max-width: 680px;">
                    <thead>
                        <tr>
                            <th style="padding: 10px; border: 1px solid #cccccc; text-align: left;">Del</th>
                            <th style="padding: 10px; border: 1px solid #cccccc; text-align: right; white-space: nowrap;">Antall</th>
                        </tr>
                    </thead>
                    <tbody>
                        $deler
                    </tbody>
                </table>

                <h2 style="font-size: 18px; margin: 24px 0 8px;">Utlevering</h2>
                <table role="presentation" style="border-collapse: collapse; width: 100%; max-width: 680px;">
                    <tbody>
                        ${nøkkelverdi("Levering", levering)}
                    </tbody>
                </table>

                <h2 style="font-size: 18px; margin: 24px 0 8px;">Bestilt av</h2>
                <table role="presentation" style="border-collapse: collapse; width: 100%; max-width: 680px;">
                    <tbody>
                        ${bestillerNavn?.trim()?.takeIf { it.isNotBlank() }?.let { nøkkelverdi("Navn", it) }.orEmpty()}
                        ${nøkkelverdi("E-post", delbestilling.epostTekniker?.trim())}
                    </tbody>
                </table>

                <h2 style="font-size: 18px; margin: 24px 0 8px;">Sak</h2>
                <table role="presentation" style="border-collapse: collapse; width: 100%; max-width: 680px;">
                    <tbody>
                        ${nøkkelverdi("Saksnummer", saksnummer.toString())}
                    </tbody>
                </table>

                <p style="margin-top: 24px;">Med vennlig hilsen<br>Nav Hjelpemidler og Tilrettelegging (HOT)</p>
            </body>
            </html>
        """.trimIndent()
    }

    private fun nøkkelverdi(nøkkel: String, verdi: String? = ""): String = """
        <tr>
            <td style="padding: 4px 0; vertical-align: top;">
                <strong>${nøkkel.escapeHtml()}:</strong> ${verdi.orEmpty().escapeHtml()}
            </td>
        </tr>
    """.trimIndent()

    private fun delrad(
        identifikator: String,
        navn: String?,
        beskrivelse: String?,
        antall: Int,
    ): String {
        val detaljer = listOfNotNull(
            navn?.takeIf { it.isNotBlank() },
            beskrivelse?.takeIf { it.isNotBlank() },
        ).joinToString("<br>") { it.escapeHtml() }
        val innhold = if (detaljer.isEmpty()) identifikator.escapeHtml()
        else "${identifikator.escapeHtml()}<br><span style=\"color: #4f4f4f;\">$detaljer</span>"

        return """
            <tr>
                <td style="padding: 10px; border: 1px solid #cccccc; text-align: left;">$innhold</td>
                <td style="padding: 10px; border: 1px solid #cccccc; text-align: right; white-space: nowrap;">$antall stk.</td>
            </tr>
        """.trimIndent()
    }
}

private fun String.escapeHtml(): String = buildString(length) {
    this@escapeHtml.forEach { char ->
        append(
            when (char) {
                '&' -> "&amp;"
                '<' -> "&lt;"
                '>' -> "&gt;"
                '"' -> "&quot;"
                '\'' -> "&#39;"
                else -> char
            }
        )
    }
}