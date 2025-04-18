package no.nav.hjelpemidler.delbestilling.infrastructure.slack

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.engine.cio.CIO
import no.nav.hjelpemidler.delbestilling.config.isProd
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingRepository
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.AnmodningsbehovForDel
import no.nav.hjelpemidler.delbestilling.delbestilling.model.Del
import no.nav.hjelpemidler.delbestilling.delbestilling.model.DelbestillingSak
import no.nav.hjelpemidler.delbestilling.delbestilling.model.Kilde
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hmsnrTilDel
import no.nav.hjelpemidler.delbestilling.infrastructure.email.enhetTilEpostadresse
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.Produkt
import no.nav.hjelpemidler.delbestilling.rapport.Hjelpemiddel
import no.nav.hjelpemidler.http.slack.slack
import no.nav.hjelpemidler.http.slack.slackIconEmoji

val log = KotlinLogging.logger { }

class Slack(
    private val delbestillingRepository: DelbestillingRepository
) {

    private val client = slack(engine = CIO.create())
    private val channel = when (isProd()) {
        true -> "#digihot-delbestillinger-alerts"
        else -> "#digihot-delbestillinger-alerts-dev"
    }
    private val username = "hm-delbestilling-api"

    private suspend fun sendSafely(emoji: String, message: String) {
        try {
            client.sendMessage(
                username = username,
                slackIconEmoji(":$emoji:"),
                channel = channel,
                message = message
            )
        } catch (e: Exception) {
            log.error(e) { "Klarte ikke sende varsle til Slack om manglende deler til grunndatahjelpemiddel " }
            // Ikke kast feil videre, ikke krise hvis denne feiler
        }
    }

    suspend fun varsleOmInnsending(
        brukerKommunenr: String,
        brukersKommunenavn: String,
        delbestillingSak: DelbestillingSak
    ) {
        try {
            val antallDelbestillingerFraKommune =
                delbestillingRepository.hentDelbestillingerForKommune(brukerKommunenr).size
            log.info { "antallDelbestillingerFraKommune for $brukersKommunenavn (brukerKommunenr: $brukerKommunenr): $antallDelbestillingerFraKommune" }

            val kommuneVåpenEmoji = ":${
                brukersKommunenavn.lowercase().replace('æ', 'e').replace('ø', 'o').replace('å', 'a').replace(' ', '_')
            }_vapen:"

            if (antallDelbestillingerFraKommune == 1) {
                sendSafely(
                    emoji = "news",
                    message = "$kommuneVåpenEmoji Ny kommune har for første gang sendt inn digital delbestilling! Denne gangen var det ${brukersKommunenavn} kommune (kommunenummer: $brukerKommunenr)"
                )
            } else if (antallDelbestillingerFraKommune == 4) {
                sendSafely(
                    emoji = "chart_with_upwards_trend",
                    message = "$kommuneVåpenEmoji Ny kommune har sendt inn 4 digitale delbestillinger! Denne gangen var det ${brukersKommunenavn} kommune (kommunenummer: $brukerKommunenr)"
                )
            }

            val delerFraGrunndata = delbestillingSak.delbestilling.deler.filter { it.del.kilde == Kilde.GRUNNDATA }
            log.info { "delerFraGrunndata: $delerFraGrunndata" }
            if (delerFraGrunndata.isNotEmpty()) {
                var message =
                    "En delbestilling har kommet inn med deler fra grunndata, i ${brukersKommunenavn} kommune! Disse delene var: ```${
                        delerFraGrunndata.joinToString("\n") { "${it.del.hmsnr} ${it.del.navn}" }
                    }```"

                val delerIManuellListe = hmsnrTilDel.values.toList()
                val delerSomOgsåFinnesIManuellListe =
                    delerFraGrunndata.filter { del -> delerIManuellListe.find { it.hmsnr == del.del.hmsnr } != null }

                message += if (delerSomOgsåFinnesIManuellListe.isNotEmpty()) {
                    "\nFølgende deler finnes også i manuell liste: ```${
                        delerSomOgsåFinnesIManuellListe.joinToString(
                            "\n"
                        ) { "${it.del.hmsnr} ${it.del.navn}" }
                    }```"
                } else {
                    "\nIngen av delene finnes i manuell liste; kun i grunndata!"
                }

                sendSafely(
                    emoji = "very_nice",
                    message = message
                )
            }
        } catch (e: Exception) {
            log.error(e) { "Klarte ikke sende varsle til Slack om innsending for kommunenr $brukerKommunenr" }
            // Ikke kast feil videre, ikke krise hvis denne feiler
        }
    }

    suspend fun varsleOmIngenDelerTilGrunndataHjelpemiddel(produkt: Produkt, delerIManuellListe: List<Del>) {
        var message =
            "Det ble gjort et oppslag på `${produkt.hmsArtNr} ${produkt.articleName}` som finnes i grunndata, men har ingen egnede deler der."
        message += if (delerIManuellListe.isNotEmpty()) {
            "\nDette produktet har disse delene i manuell liste: ```${
                delerIManuellListe.sortedBy { it.navn }.joinToString("\n") { "${it.hmsnr} ${it.navn}" }
            }```"
        } else {
            "\nDette produktet har heller ingen deler i manuell liste."
        }

        sendSafely(emoji = "sadcat", message = message)
    }

    suspend fun varsleOmInnsendingFeilet(correlationId: String) {
        val url =
            """https://logs.adeo.no/app/discover#/?_g=(filters:!(),refreshInterval:(pause:!t,value:60000),time:(from:now-2d,to:now))&_a=(columns:!(level,message,envclass,application,pod),dataSource:(dataViewId:'96e648c0-980a-11e9-830a-e17bbd64b4db',type:dataView),filters:!(),hideChart:!f,interval:auto,query:(language:kuery,query:'application:%22hm-delbestilling-api%22%20and%20envclass:%22p%22%20%20and%20x_correlationId:%22${correlationId}%22'),sort:!(!('@timestamp',desc)))"""
        sendSafely(
            emoji = "this-is-fine-fire",
            message = "En innsending av en delbestilling feilet (correlationId: $correlationId). Sjekk loggene her: ${url}"
        )
    }

    suspend fun rapporterAntallBestillingerOgUtlånForHjelpemidler(hjelpemiddel: List<Hjelpemiddel>) = sendSafely(
        emoji = "clippy",
        message = "Antall utlån og delbestillinger per hjelpemiddel: ${
            hjelpemiddel.joinToString(separator = ",") { "(${it.hmnsr},${it.utlån},${it.bestillinger})" }
        }"
    )

    suspend fun varsleGrunndataDekkerManuellListeForHjelpemiddel(hmsnr: String, navn: String) = sendSafely(
        emoji = "pepe-peek",
        message = "Hjelpemiddelet $hmsnr '$navn' har alle deler fra manuell liste i grunndata også. Det kan dermed fjernes fra den manuelle listen :broom:"
    )

    suspend fun varsleOmDelerUtenDekning(
        deler: List<AnmodningsbehovForDel>,
        brukersKommunenavn: String,
        enhetnr: String
    ) = sendSafely(
        emoji = "pepe_cowboy",
        message = """
            Det har kommet inn delbestilling med følgende deler som ikke har dekning hos enhet $enhetnr (kommune: ${brukersKommunenavn}):
            ```${deler.joinToString("\n")}```
            Disse må kanskje anmodes, ny sjekk gjøres i natt.
            """.trimIndent(),
    )

    suspend fun varsleOmManglendeHmsnr(hmsnr: String) = sendSafely(
        emoji = "thinkies",
        message = "Det ble gjort et oppslag på `$hmsnr`, men dette er et produkt som verken finnes i manuell liste eller i grunndata."
    )

    suspend fun varsleOmAnmodningrapportSomErSendtTilEnhet(enhetnr: String, melding: String) {
        val tilEpost = enhetTilEpostadresse(enhetnr)
        sendSafely(
            emoji = "mailbox",
            message = """
                Følgende mail ble sendt til enhet $enhetnr ($tilEpost):
                ```
                $melding
                ```
                """.trimIndent()
        )
    }

    suspend fun varsleOmIngenAnmodninger() = sendSafely(
        emoji = "such-empty",
        message = "Ingen anmodningsrapporter ble sendt ut; alle bestilte deler har hatt lagerdekning."
    )

    suspend fun varsleOmRapporteringFeilet() = sendSafely(
        emoji = "error",
        message = "Utsending av mail til HMS om deler som må anmodes feilet. Må følges opp manuelt."
    )

}