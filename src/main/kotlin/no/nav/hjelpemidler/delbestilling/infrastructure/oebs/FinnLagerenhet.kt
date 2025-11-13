package no.nav.hjelpemidler.delbestilling.infrastructure.oebs

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.common.Lager
import no.nav.hjelpemidler.delbestilling.infrastructure.norg.Norg
import no.nav.hjelpemidler.delbestilling.infrastructure.slack.Slack

private val log = KotlinLogging.logger { }

/**
 * TODO Logikk for å mappe fra enhet/geografi til lager bør flyttes til en felles plass for DigiHoT, eller hentes fra OeBS.
 * Logikken kan endre seg over tid dersom det f.eks. blir endring i sammenslåing av sentralene.
 */
class FinnLagerenhet(
    private val norg: Norg,
    private val slack: Slack,
) {
    suspend operator fun invoke(kommunenummer: String): Lager {
        val hmsEnhet = norg.hentEnhetnummer(kommunenummer)
        val lagerenhet = try {
            when (hmsEnhet) {

                /*
                4702 = HMS Akershus, men når det gjelder lagerstatus så håndteres dette av Oslo.
                I OeBS skal innbyggere her være knyttet til 4703 HMS Oslo
                 */
                ENHETSNUMMER_HMS_AKERSHUS -> Lager.OSLO

                /*
                For HMS Troms og Finnmark så bruker man lager for hvert fylke. Altså ett lager per fylke.
                 */
                ENHETSNUMMER_HMS_TROMS_OG_FINNMARK -> {
                    if (kommunenummer.erFinnmark()) {
                        Lager.FINNMARK
                    } else {
                        Lager.TROMS
                    }
                }

                /*
                For HMS Trøndelag så er det to lagre, henholdsvis Nord -og Sør-Trøndelag. Sjekk derfor kommunenummer.
                 */
                ENHETSNUMMER_HMS_TRØNDELAG -> {
                    if (kommunenummer.tilhørerNordTrøndelagLager()) {
                        log.info { "Enhetnr $hmsEnhet og kommunenummer $kommunenummer tilhører LAGER.NORD_TRØNDELAG" }
                        Lager.NORD_TRØNDELAG
                    } else {
                        log.info { "Enhetnr $hmsEnhet og kommunenummer $kommunenummer tilhører LAGER.SØR_TRØNDELAG" }
                        Lager.SØR_TRØNDELAG
                    }
                }

                // TODO Sjekk om Agder må håndteres også

                else -> Lager.fraLagernummer(hmsEnhet)
            }
        } catch (e: Exception) {
            log.error(e) { "Klarte ikke finne enhet for kommunenummer $kommunenummer" }
            slack.varsleOmUkjentEnhet(kommunenummer, hmsEnhet)
            throw e
        }

        log.info { "Fant lagerenhet $lagerenhet for kommune $kommunenummer (hmsEnhet=$hmsEnhet)." }
        return lagerenhet
    }
}

const val ENHETSNUMMER_HMS_AKERSHUS = "4702"
const val ENHETSNUMMER_HMS_TROMS_OG_FINNMARK = "4719"
const val ENHETSNUMMER_HMS_TRØNDELAG = "4716"

private const val KOMMUNENUMMER_PREFIX_TROMS = "55"
private fun String.erTroms() = this.take(2) == KOMMUNENUMMER_PREFIX_TROMS

private const val KOMMUNENUMMER_PREFIX_FINNMARK = "56"
private fun String.erFinnmark() = this.take(2) == KOMMUNENUMMER_PREFIX_FINNMARK
private fun String.tilhørerNordTrøndelagLager() = kommunerSomTilhørerNordTrøndelagLager.contains(this)

private val kommunerSomTilhørerNordTrøndelagLager = listOf(
    "5060",
    "5053",
    "5052",
    "5051",
    "5050",
    "5049",
    "5048",
    "5047",
    "5046",
    "5045",
    "5044",
    "5043",
    "5042",
    "5041",
    "5040",
    "5039",
    "5038",
    "5037",
    "5020",
    "5007",
    "5006",
    "5005",
    "5004",
)