package no.nav.hjelpemidler.delbestilling.slack

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.engine.cio.CIO
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingRepository
import no.nav.hjelpemidler.delbestilling.isProd
import no.nav.hjelpemidler.http.slack.slack
import no.nav.hjelpemidler.http.slack.slackIconEmoji

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

    suspend fun varsleOmInnsending(brukerKommunenr: String, brukersKommunenavn: String) {
        try {
            val antallDelbestillingerFraKommune = delbestillingRepository.hentDelbestillingerForKommune(brukerKommunenr).size
            log.info { "antallDelbestillingerFraKommune for brukerKommunenr $brukerKommunenr: $antallDelbestillingerFraKommune" }
            if (true) {
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
        } catch (e: Exception) {
            log.error(e) { "Klarte ikke sende varsle til Slack om innsending for kommunenr $brukerKommunenr" }
            // Ikke kast feil videre, ikke krise hvis denne feiler
        }
    }
}