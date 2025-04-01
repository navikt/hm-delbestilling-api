package no.nav.hjelpemidler.delbestilling.slack

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.engine.cio.CIO
import no.nav.hjelpemidler.delbestilling.delbestilling.Del
import no.nav.hjelpemidler.delbestilling.delbestilling.DelUtenDekning
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingRepository
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingSak
import no.nav.hjelpemidler.delbestilling.delbestilling.Kilde
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hmsnrTilDel
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.Produkt
import no.nav.hjelpemidler.delbestilling.isProd
import no.nav.hjelpemidler.delbestilling.rapport.Hjelpemiddel
import no.nav.hjelpemidler.http.slack.slack
import no.nav.hjelpemidler.http.slack.slackIconEmoji
import java.time.LocalDate

val log = KotlinLogging.logger { }

class SlackClient(
    private val delbestillingRepository: DelbestillingRepository
) {
    private val slackClient by lazy { slack(engine = CIO.create()) }
    private val channel = when (isProd()) {
        true -> "#digihot-delbestillinger-alerts"
        else -> "#digihot-delbestillinger-alerts-dev"
    }
    private val username = "hm-delbestilling-api"

    suspend fun varsleOmInnsending(
        brukerKommunenr: String,
        brukersKommunenavn: String,
        delbestillingSak: DelbestillingSak
    ) {
        try {
            val antallDelbestillingerFraKommune =
                delbestillingRepository.hentDelbestillingerForKommune(brukerKommunenr).size
            log.info { "antallDelbestillingerFraKommune for $brukersKommunenavn (brukerKommunenr: $brukerKommunenr): $antallDelbestillingerFraKommune" }
            if (antallDelbestillingerFraKommune == 1) {
                slackClient.sendMessage(
                    username = username,
                    slackIconEmoji(":news:"),
                    channel = channel,
                    message = "Ny kommune har for første gang sendt inn digital delbestilling! Denne gangen var det ${brukersKommunenavn} kommune (kommunenummer: $brukerKommunenr)"
                )
            } else if (antallDelbestillingerFraKommune == 4) {
                slackClient.sendMessage(
                    username = username,
                    slackIconEmoji(":chart_with_upwards_trend:"),
                    channel = channel,
                    message = "Ny kommune har sendt inn 4 digitale delbestillinger! Denne gangen var det ${brukersKommunenavn} kommune (kommunenummer: $brukerKommunenr)"
                )
            }

            val delerFraUtvidetSortiment19Feb =
                delbestillingSak.delbestilling.deler.filter { it.del.datoLagtTil == LocalDate.of(2025, 2, 19) }
            log.info { "delerFraUtvidetSortiment19Feb: $delerFraUtvidetSortiment19Feb" }
            if (delerFraUtvidetSortiment19Feb.isNotEmpty()) {
                slackClient.sendMessage(
                    username = username,
                    slackIconEmoji(":tada:"),
                    channel = channel,
                    message = "En delbestilling har kommet inn med nye deler som ble lagt til 19 februar, i ${brukersKommunenavn} kommune! Disse delene var: ${
                        delerFraUtvidetSortiment19Feb.joinToString(
                            ", "
                        ) { "${it.del.hmsnr} ${it.del.navn}" }
                    }"
                )
            }

            val delerFraGrunndata = delbestillingSak.delbestilling.deler.filter { it.del.kilde == Kilde.GRUNNDATA }
            log.info { "delerFraGrunndata: $delerFraGrunndata" }
            if (delerFraGrunndata.isNotEmpty()) {
                var message =
                    "En delbestilling har kommet inn med deler fra grunndata, i ${brukersKommunenavn} kommune! Disse delene var: ${
                        delerFraGrunndata.joinToString(", ") { "`${it.del.hmsnr} ${it.del.navn}`" }
                    }"

                val delerIManuellListe = hmsnrTilDel.values.toList()
                val delerSomOgsåFinnesIManuellListe =
                    delerFraGrunndata.filter { del -> delerIManuellListe.find { it.hmsnr == del.del.hmsnr } != null }

                message += if (delerSomOgsåFinnesIManuellListe.isNotEmpty()) {
                    ".\nFølgende deler finnes også i manuell liste: ```${
                        delerSomOgsåFinnesIManuellListe.joinToString(
                            "\n"
                        ) { "${it.del.hmsnr} ${it.del.navn}" }
                    }```"
                } else {
                    "\nIngen av delene finnes i manuell liste; kun i grunndata!"
                }

                slackClient.sendMessage(
                    username = username,
                    slackIconEmoji(":very_nice:"),
                    channel = channel,
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
        try {
            slackClient.sendMessage(
                username = username,
                slackIconEmoji(":sadcat:"),
                channel = channel,
                message = message
            )
        } catch (e: Exception) {
            log.error(e) { "Klarte ikke sende varsle til Slack om manglende deler til grunndatahjelpemiddel " }
            // Ikke kast feil videre, ikke krise hvis denne feiler
        }
    }

    suspend fun varsleOmInnsendingFeilet(correlationId: String) {
        try {
            val url = """
            https://logs.adeo.no/app/discover#/?_g=(filters:!(),refreshInterval:(pause:!t,value:60000),time:(from:now-2d,to:now))&_a=(columns:!(level,message,envclass,application,pod),dataSource:(dataViewId:'96e648c0-980a-11e9-830a-e17bbd64b4db',type:dataView),filters:!(),hideChart:!f,interval:auto,query:(language:kuery,query:'application:%22hm-delbestilling-api%22%20and%20envclass:%22p%22%20%20and%20x_correlationId:%22${correlationId}%22'),sort:!(!('@timestamp',desc)))
        """.trimIndent()
            slackClient.sendMessage(
                username = username,
                slackIconEmoji(":this-is-fine-fire:"),
                channel = channel,
                message = "En innsending av en delbestilling feilet (correlationId: $correlationId). Sjekk loggene her: ${url}"
            )
        } catch (e: Exception) {
            log.error(e) { "Klarte ikke sende varsle til Slack om feilende innsending av delbestilling" }
            // Ikke kast feil videre, ikke krise hvis denne feiler
        }
    }

    suspend fun rapporterAntallBestillingerOgUtlånForHjelpemidler(hjelpemiddel: List<Hjelpemiddel>) {
        slackClient.sendMessage(
            username = username,
            slackIconEmoji(":clippy:"),
            channel = channel,
            message = "Antall utlån og delbestillinger per hjelpemiddel: ${
                hjelpemiddel.joinToString(separator = ",") { "(${it.hmnsr},${it.utlån},${it.bestillinger})" }
            }"
        )
    }

    suspend fun varsleGrunndataDekkerManuellListeForHjelpemiddel(hmsnr: String, navn: String) {
        slackClient.sendMessage(
            username = username,
            slackIconEmoji(":pepe-peek:"),
            channel = channel,
            message = "Hjelpemiddelet $hmsnr '$navn' har alle deler fra manuell liste i grunndata også. Det kan dermed fjernes fra den manuelle listen :broom:"
        )
    }

    suspend fun rapporterOmDelerUtenDekning(delerUtenDekning: List<DelUtenDekning>, brukersKommunenavn: String, enhetnr: String) {
        try {
            slackClient.sendMessage(
                username = username,
                slackIconEmoji(":pepe_cowboy:"),
                channel = channel,
                message = "Det har kommet inn delbestilling med følgende deler som ikke har dekning hos enhet $enhetnr (kommune: ${brukersKommunenavn}):```${delerUtenDekning.joinToString { "\n${it.hmsnr} ${it.navn} (${it.antall}stk)" }}```",
            )
        }  catch (e: Exception) {
            log.error(e) { "Klarte ikke sende varsle til Slack deler uten dekning" }
            // Ikke kast feil videre, ikke krise hvis denne feiler
        }
    }
}