package no.nav.hjelpemidler.delbestilling.delbestilling

import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Oebs
import no.nav.hjelpemidler.hjelpemidlerdigitalSoknadapi.tjenester.norg.NorgService
import kotlin.math.abs

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
                return@mapNotNull null
            }

            val antallPåLager = lagerstatus.antallDelerPåLager
            if (antallPåLager > delLinje.antall) {
                // Flere på lager enn det er bestilt
                return@mapNotNull null
            }

            val antallIkkePåLager = abs(antallPåLager - delLinje.antall)
            DelUtdenDekning(
                hmsnr = delLinje.del.hmsnr,
                navn = delLinje.del.navn,
                antall = antallIkkePåLager
            )
        }

        val enhet = norgService.hentHmsEnhet(sak.brukersKommunenummer)

        delerUtenDekning.forEach { del ->
            repository.lagreDelerUtenDekning(
                tx = tx,
                saksnummer = sak.saksnummer,
                hmsnr = del.hmsnr,
                navn = del.navn,
                antallUtenDekning =del.antall,
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