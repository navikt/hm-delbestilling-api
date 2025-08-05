package no.nav.hjelpemidler.delbestilling.infrastructure.slack

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import no.nav.hjelpemidler.delbestilling.common.DelbestillingSak
import no.nav.hjelpemidler.delbestilling.common.Enhet
import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.common.Kilde
import no.nav.hjelpemidler.delbestilling.config.isProd
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.AnmodningsbehovForDel
import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction.Transactional
import no.nav.hjelpemidler.delbestilling.oppslag.Del
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.data.hmsnrTilDel
import no.nav.hjelpemidler.http.slack.slack
import no.nav.hjelpemidler.http.slack.slackIconEmoji

private val log = KotlinLogging.logger { }

class Slack(
    private val transaction: Transactional,
    private val scope: CoroutineScope,
) {

    private val client = slack(engine = CIO.create())
    private val channel = when (isProd()) {
        true -> "#digihot-delbestillinger-alerts"
        else -> "#digihot-delbestillinger-alerts-dev"
    }
    private val username = "hm-delbestilling-api"

    private fun sendSafely(emoji: String, message: String) = scope.launch(Dispatchers.IO) {
        try {
            client.sendMessage(
                username = username,
                slackIconEmoji(":$emoji:"),
                channel = channel,
                message = message
            )
        } catch (e: Exception) {
            log.error(e) { "Klarte ikke sende varsel til Slack" }
            // Ikke kast feil videre, ikke krise hvis denne feiler
        }
    }

    suspend fun varsleOmInnsending(
        brukerKommunenr: String,
        brukersKommunenavn: String,
    ) {
        try {
            val antallDelbestillingerFraKommune = transaction {
                delbestillingRepository.hentDelbestillingerForKommune(brukerKommunenr).size
            }
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
        } catch (e: Exception) {
            log.error(e) { "Klarte ikke sende varsle til Slack om innsending for kommunenr $brukerKommunenr" }
            // Ikke kast feil videre, ikke krise hvis denne feiler
        }
    }

    fun varsleOmInnsendingFeilet(correlationId: String) {
        sendSafely(
            emoji = "this-is-fine-fire",
            message = "En innsending av en delbestilling feilet (correlationId: `$correlationId`). Sjekk loggene."
        )
    }

    fun varsleGrunndataDekkerManuellListeForHjelpemiddel(hmsnr: String, navn: String) = sendSafely(
        emoji = "pepe-peek",
        message = "Hjelpemiddelet $hmsnr '$navn' har alle deler fra manuell liste i grunndata også. Det kan dermed fjernes fra den manuelle listen :broom:"
    )

    fun varsleOmDelerUtenDekning(
        deler: List<AnmodningsbehovForDel>,
        brukersKommunenavn: String,
        enhetnr: String
    ) {
        val enhet = runCatching { Enhet.fraEnhetsnummer(enhetnr) }.getOrNull()?.name
        sendSafely(
            emoji = "pepe_cowboy",
            message = """
            Det har kommet inn delbestilling med følgende deler som ikke har dekning hos enhet $enhet (kommune: ${brukersKommunenavn}):
            ```${deler.joinToString("\n")}```
            Disse må kanskje anmodes, ny sjekk gjøres i natt.
            """.trimIndent(),
        )
    }

    fun varsleOmAnmodningrapportSomErSendtTilEnhet(enhet: Enhet, melding: String) {
        sendSafely(
            emoji = "mailbox",
            message = """
                Følgende mail ble sendt til enhet $enhet (${enhet.epost()}):
                ```
                $melding
                ```
                """.trimIndent()
        )
    }

    fun varsleOmIngenAnmodninger() = sendSafely(
        emoji = "such-empty",
        message = "Ingen anmodningsrapporter ble sendt ut; alle bestilte deler har hatt lagerdekning."
    )

    fun varsleOmRapporteringFeilet() = sendSafely(
        emoji = "error",
        message = "Utsending av mail til HMS om deler som må anmodes feilet. Må følges opp manuelt."
    )

    fun varsleOmPotensiellBatteriKategorier(deler: List<Del>) = sendSafely(
        emoji = "low_battery",
        message = "Følgende deler med 'batteri' i kategorien sin har blitt bestilt. Vurder om de krever kurs eller skal legges inn som 'håndterteBatterikategorier'. ```${deler.joinToString(separator = "\n")}```"
    )

    fun varsleOmUkjentEnhet(kommunenummer: String, enhetsnummer: String) = sendSafely(
        emoji = "error",
        message = "Enhet er ikke definert for enhetsnummer=$enhetsnummer (kommunenummer=$kommunenummer)"
    )

}