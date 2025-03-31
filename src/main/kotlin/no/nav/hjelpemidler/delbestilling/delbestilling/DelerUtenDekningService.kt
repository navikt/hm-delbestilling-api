package no.nav.hjelpemidler.delbestilling.delbestilling

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Oebs
import no.nav.hjelpemidler.hjelpemidlerdigitalSoknadapi.tjenester.norg.NorgService
import kotlin.math.abs

private val log = KotlinLogging.logger {}

class DelerUtenDekningService(
    private val repository: DelerUtenDekningRepository,
    private val oebs: Oebs,
    private val norgService: NorgService,
) {
    suspend fun lagreDelerUtenDekning(sak: DelbestillingSak, tx: JdbcOperations) {
        val hmsnrDeler = sak.delbestilling.deler.map { it.del.hmsnr }
        val lagerstatuser = oebs.hentLagerstatus(sak.brukersKommunenummer, hmsnrDeler).associateBy { it.artikkelnummer }
        val delerUtenDekning = sak.delbestilling.deler.mapNotNull { delLinje ->
            val lagerstatus = requireNotNull(lagerstatuser[delLinje.del.hmsnr])

            if (lagerstatus.minmax) {
                // Dersom delen er på minmax så har den dekning
                log.info { "${delLinje.del.hmsnr} er på minmax, har dermed dekning" }
                return@mapNotNull null
            }

            val antallPåLager = lagerstatus.antallDelerPåLager
            if (antallPåLager > delLinje.antall) {
                // Flere på lager enn det er bestilt
                log.info { "${delLinje.del.hmsnr} er det flere av på lager (${antallPåLager}) enn bestilt (${delLinje.antall}), har dermed dekning" }
                return@mapNotNull null
            }

            val antallIkkePåLager = abs(antallPåLager - delLinje.antall)
            log.info { "antallIkkePåLager for ${delLinje.del.hmsnr}: $antallIkkePåLager" }

            DelUtdenDekning(
                hmsnr = delLinje.del.hmsnr,
                navn = delLinje.del.navn,
                antall = antallIkkePåLager
            )
        }

        val enhet = norgService.hentHmsEnhet(sak.brukersKommunenummer)

        log.info { "Lagrer følgende delerUtenDekning: $delerUtenDekning" }

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