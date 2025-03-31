package no.nav.hjelpemidler.delbestilling.delbestilling

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Oebs
import no.nav.hjelpemidler.delbestilling.isDev
import no.nav.hjelpemidler.delbestilling.slack.SlackClient
import no.nav.hjelpemidler.hjelpemidlerdigitalSoknadapi.tjenester.norg.NorgService
import kotlin.math.abs

private val log = KotlinLogging.logger {}

class DelerUtenDekningService(
    private val repository: DelerUtenDekningRepository,
    private val oebs: Oebs,
    private val norgService: NorgService,
    private val slackClient: SlackClient,
) {
    suspend fun lagreDelerUtenDekning(sak: DelbestillingSak, tx: JdbcOperations) {
        val hmsnrDeler = sak.delbestilling.deler.map { it.del.hmsnr }
        val lagerstatuser = oebs.hentLagerstatus(sak.brukersKommunenummer, hmsnrDeler).associateBy { it.artikkelnummer }
        val enhet = norgService.hentHmsEnhet(sak.brukersKommunenummer)

        val delerUtenDekning = sak.delbestilling.deler.mapNotNull { delLinje ->
            val lagerstatus = requireNotNull(lagerstatuser[delLinje.del.hmsnr])

            if (isDev()) {
                log.info { "Dekningsjekk: lagerstatus for ${delLinje.del.hmsnr}: $lagerstatus" }
            }

            if (lagerstatus.minmax) {
                // Dersom delen er på minmax så har den dekning
                log.info { "Dekningsjekk: ${delLinje.del.hmsnr} er på minmax hos enhet ${enhet.enhetNr}, har dermed dekning" }
                return@mapNotNull null
            }

            val antallPåLager = lagerstatus.antallDelerPåLager
            if (antallPåLager > delLinje.antall) {
                // Flere på lager enn det er bestilt
                log.info { "Dekningsjekk: ${delLinje.del.hmsnr} er det flere av på lager (${antallPåLager}) enn bestilt (${delLinje.antall}) hos enhet ${enhet.enhetNr}, har dermed dekning" }
                return@mapNotNull null
            }

            val antallIkkePåLager = abs(antallPåLager - delLinje.antall)
            log.info { "Dekningsjekk: antallIkkePåLager for ${delLinje.del.hmsnr}: $antallIkkePåLager hos enhet ${enhet.enhetNr}" }

            DelUtdenDekning(
                hmsnr = delLinje.del.hmsnr,
                navn = delLinje.del.navn,
                antall = antallIkkePåLager
            )
        }

        if (delerUtenDekning.isNotEmpty()) {
            slackClient.rapporterOmDelerUtenDekning(delerUtenDekning, sak.brukersKommunenavn, enhet.enhetNr)
        }

        log.info { "Dekningsjekk: lagrer følgende delerUtenDekning: $delerUtenDekning" }

        delerUtenDekning.forEach { del ->
            repository.lagreDelerUtenDekning(
                tx = tx,
                saksnummer = sak.saksnummer,
                hmsnr = del.hmsnr,
                navn = del.navn,
                antallUtenDekning = del.antall,
                bukersKommunenummer = sak.brukersKommunenummer,
                brukersKommunenavn = sak.brukersKommunenavn,
                enhetnr = enhet.enhetNr,
            )
        }
    }
}

data class DelUtdenDekning (
    val hmsnr: String,
    val navn: String,
    val antall: Int,
)