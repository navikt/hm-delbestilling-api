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
        val lagerstatuser = oebs.hentLagerstatusForKommunenummer(sak.brukersKommunenummer, hmsnrDeler).associateBy { it.artikkelnummer }
        val enhet = norgService.hentHmsEnhet(sak.brukersKommunenummer)

        val delerUtenDekning = sak.delbestilling.deler.mapNotNull { delLinje ->
            val lagerstatus = requireNotNull(lagerstatuser[delLinje.del.hmsnr])

            if (isDev()) {
                log.info { "Dekningsjekk: lagerstatus for ${delLinje.del.hmsnr} for enhet ${enhet.enhetNr}: $lagerstatus" }
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

            DelUtenDekning(
                hmsnr = delLinje.del.hmsnr,
                navn = delLinje.del.navn,
                antall = antallIkkePåLager,
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

    suspend fun hentDagensDelerUtenDekning() {
        // Hent først alle unike enhetnr
        val enhetnrs = repository.hentUnikeEnhetnrs()
        log.info { "Rapporterer dagens dekning uten deler" }
        log.info { "enhetnrs: $enhetnrs" }

        enhetnrs.forEach {enhetnr ->
            val potensielleDelerUtenDekning = repository.hentDagensDelerUtenDekning(enhetnr)
            log.info { "delerUtenDekning for enhet $enhetnr: $potensielleDelerUtenDekning" }

            val lagerstatuser = oebs.hentLagerstatusForEnhetnr(enhetnr = enhetnr, hmsnrs = potensielleDelerUtenDekning.map { it.hmsnr }).associateBy { it.artikkelnummer }

            val delerUtenDekning = potensielleDelerUtenDekning.mapNotNull {delUtenDekning ->
                // Sjekk lagerstatus på nytt
                val lagerstatus = requireNotNull(lagerstatuser[delUtenDekning.hmsnr])

                // TODO: se hva som kan gjenbrukes
                if (isDev()) {
                    log.info { "Dekningsjekk: lagerstatus for ${delUtenDekning.hmsnr} for enhet ${enhetnr}: $lagerstatus" }
                }

                if (lagerstatus.minmax) {
                    // Dersom delen er på minmax så har den dekning
                    log.info { "Dekningsjekk: ${delUtenDekning.hmsnr} er på minmax hos enhet ${enhetnr}, har dermed dekning" }
                    return@mapNotNull null
                }

                val antallPåLager = lagerstatus.antallDelerPåLager
                if (antallPåLager > delUtenDekning.antall) {
                    // Flere på lager enn det er bestilt
                    log.info { "Dekningsjekk: ${delUtenDekning.hmsnr} er det flere av på lager (${antallPåLager}) enn bestilt (${delUtenDekning.antall}) hos enhet ${enhetnr}, har dermed dekning" }
                    return@mapNotNull null
                }

                val antallIkkePåLager = abs(antallPåLager - delUtenDekning.antall)
                log.info { "Dekningsjekk: antallIkkePåLager for ${delUtenDekning.hmsnr}: $antallIkkePåLager hos enhet ${enhetnr}" }

                DelUtenDekning(
                    hmsnr = delUtenDekning.hmsnr,
                    navn = delUtenDekning.navn,
                    antall = antallIkkePåLager,
                )
            }

            val melding = delerUtenDekning.joinToString("\n") { "${it.hmsnr} ${it.navn} ${it.antall}" }
            log.info { "melding: $melding" }
        }
    }
}

data class DelUtenDekning (
    val hmsnr: String,
    val navn: String,
    val antall: Int,
)