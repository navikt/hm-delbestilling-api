package no.nav.hjelpemidler.delbestilling.delbestilling.ikkeSkipedeDelbestillinger

import no.nav.hjelpemidler.delbestilling.common.DelbestillingSak
import no.nav.hjelpemidler.delbestilling.common.Lager

data class IkkeSkipetDelbestillingerRapport(
    val lager: Lager,
    val delbestillinger: List<DelbestillingSak>,
) {
    fun tilHtml(): String {
        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>TEST: ikke-skipede delbestillinger</title>
                <style>
                    table {
                        width: 100%;
                        border-collapse: collapse;
                    }
                    th, td {
                        padding: 10px;
                        border: 1px solid #ccc;
                        text-align: left;
                    }
                </style>
            </head>
            <body>
                <p>
                    Her er en oversikt over ordre som er eldre enn 30 dager som fortsatt har status KLARGJORT i OeBS.
                    Dere vurderer om det trengs å gjøres noe spesielt med disse.
                    </br>
                    </br>
                    HMS lager: ${this.lager.navn} </br>
                </p>
                <table>
                    <thead>
                        <tr>
                            <th>Opprettet</th>
                            <th>OeBS-ordrenummer</th>
                            <th>Deler</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${this.delbestillinger.sortedByDescending { it.opprettet }.joinToString("") {
                            """
                                <tr>
                                    <td>${it.opprettet.toLocalDate()}</td>
                                    <td>${it.oebsOrdrenummer}</td>
                                    <td>${it.delbestilling.deler.joinToString("<br/>") {delLinje -> "${delLinje.del.hmsnr} ${delLinje.del.navn} (${delLinje.antall}stk)" }}</td>
                                </tr>
                            """.trimIndent()
                        }}
                    </tbody>
                </table>
            </body>
            </html>
        """.trimIndent()

        return html
    }
}