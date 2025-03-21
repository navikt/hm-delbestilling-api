package no.nav.hjelpemidler.delbestilling.slack

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.engine.cio.CIO
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingRepository
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingSak
import no.nav.hjelpemidler.delbestilling.delbestilling.HjelpemiddelMedDeler
import no.nav.hjelpemidler.delbestilling.delbestilling.Kilde
import no.nav.hjelpemidler.delbestilling.grunndata.Produkt
import no.nav.hjelpemidler.delbestilling.isProd
import no.nav.hjelpemidler.http.slack.slack
import no.nav.hjelpemidler.http.slack.slackIconEmoji
import java.time.LocalDate

val log = KotlinLogging.logger { }

class SlackClient(
    private val delbestillingRepository: DelbestillingRepository
) {
    private val slackClient by lazy { slack(engine = CIO.create()) }
    private val channel = when(isProd()) {
        true -> "#digihot-delbestillinger-alerts"
        else -> "#digihot-delbestillinger-alerts-dev"
    }
    private val username = "hm-delbestilling-api"

    suspend fun varsleOmInnsending(brukerKommunenr: String, brukersKommunenavn: String, delbestillingSak: DelbestillingSak) {
        try {
            val antallDelbestillingerFraKommune = delbestillingRepository.hentDelbestillingerForKommune(brukerKommunenr).size
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

            val delerFraUtvidetSortiment19Feb = delbestillingSak.delbestilling.deler.filter { it.del.datoLagtTil == LocalDate.of(2025, 2, 19) }
            log.info { "delerFraUtvidetSortiment19Feb: $delerFraUtvidetSortiment19Feb" }
            if (delerFraUtvidetSortiment19Feb.isNotEmpty()) {
                slackClient.sendMessage(
                    username = username,
                    slackIconEmoji(":tada:"),
                    channel = channel,
                    message = "En delbestilling har kommet inn med nye deler som ble lagt til 19 februar, i ${brukersKommunenavn} kommune! Disse delene var: ${delerFraUtvidetSortiment19Feb.joinToString(", ") { "${it.del.hmsnr} ${it.del.navn}" }}"
                )
            }

            val delerFraGrunndata = delbestillingSak.delbestilling.deler.filter { it.del.kilde == Kilde.GRUNNDATA }
            log.info { "delerFraGrunndata: $delerFraGrunndata" }
            if (delerFraGrunndata.isNotEmpty()) {
                slackClient.sendMessage(
                    username = username,
                    slackIconEmoji(":very_nice:"),
                    channel = channel,
                    message = "En delbestilling har kommet inn med deler fra grunndata, i ${brukersKommunenavn} kommune! Disse delene var: ${delerFraGrunndata.joinToString(", ") { "${it.del.hmsnr} ${it.del.navn}" }}"
                )
            }
        } catch (e: Exception) {
            log.error(e) { "Klarte ikke sende varsle til Slack om innsending for kommunenr $brukerKommunenr" }
            // Ikke kast feil videre, ikke krise hvis denne feiler
        }
    }

    suspend fun varsleOmIngenDelerTilGrunndataHjelpemiddel(produkt: Produkt) {
        slackClient.sendMessage(
            username = username,
            slackIconEmoji(":sadcat:"),
            channel = channel,
            message = "Det ble gjort et oppslag på `${produkt.hmsArtNr} ${produkt.articleName}` som finnes i grunndata, men har ingen egnede deler der. Kanskje noe å se på?"
        )
    }

    suspend fun varsleOmInnsendingFeilet(correlationId: String) {
        val url = """
            https://logs.adeo.no/app/discover#/?_g=(filters:!(),refreshInterval:(pause:!t,value:60000),time:(from:now-2d,to:now))&_a=(columns:!(level,message,envclass,application,pod),dataSource:(dataViewId:'96e648c0-980a-11e9-830a-e17bbd64b4db',type:dataView),filters:!(),hideChart:!f,interval:auto,query:(language:kuery,query:'application:%22hm-delbestilling-api%22%20and%20envclass:%22p%22%20%20and%20x_correlationId:%22${correlationId}%22'),sort:!(!('@timestamp',desc)))
        """.trimIndent()
        slackClient.sendMessage(
            username = username,
            slackIconEmoji(":this-is-fine-fire:"),
            channel = channel,
            message = "En innsending av en delbestilling feilet (correlationId: $correlationId). Sjekk loggene her: ${url}"
        )
    }
}