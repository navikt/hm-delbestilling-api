package no.nav.hjelpemidler.delbestilling.delbestilling.anmodning

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingSak
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Oebs
import no.nav.hjelpemidler.delbestilling.slack.SlackClient
import no.nav.hjelpemidler.hjelpemidlerdigitalSoknadapi.tjenester.norg.NorgService

private val log = KotlinLogging.logger {}

class AnmodningService(
    private val repository: AnmodningRepository,
    private val oebs: Oebs,
    private val norgService: NorgService,
    private val slackClient: SlackClient,
) {

    suspend fun lagreDelerTilAnmodning(sak: DelbestillingSak, tx: JdbcOperations) {
        val hmsnrDeler = sak.delbestilling.deler.map { it.del.hmsnr }
        val lagerstatuser = oebs.hentLagerstatusForKommunenummer(sak.brukersKommunenummer, hmsnrDeler)
            .associateBy { it.artikkelnummer }
        val enhet = norgService.hentHmsEnhet(sak.brukersKommunenummer)

        val delerSomMåAnmodes = sak.delbestilling.deler.map { dellinje ->
            val lagerstatus = requireNotNull(lagerstatuser[dellinje.del.hmsnr])
            beregnAnmodningsbehovForDel(dellinje, lagerstatus)
        }.filter { it.antallSomMåAnmodes > 0 }

        if (delerSomMåAnmodes.isNotEmpty()) {
            slackClient.rapporterOmDelerUtenDekning(delerSomMåAnmodes, sak.brukersKommunenavn, enhet.enhetNr)
        }

        log.info { "Dekningsjekk: lagrer følgende deler som må anmodes: ${delerSomMåAnmodes.joinToString("\n")}" }

        delerSomMåAnmodes.forEach { del ->
            repository.lagreDelerUtenDekning(
                tx = tx,
                saksnummer = sak.saksnummer,
                hmsnr = del.hmsnr,
                navn = del.navn,
                antallUtenDekning = del.antallSomMåAnmodes,
                bukersKommunenummer = sak.brukersKommunenummer,
                brukersKommunenavn = sak.brukersKommunenavn,
                enhetnr = enhet.enhetNr,
            )
        }
    }

    suspend fun genererAnmodningsrapporter(): List<Anmodningrapport> {
        log.info { "Genererer rapport for bestilte deler som må anmodes" }

        // Hent først alle unike enhetnr
        val hmsEnheter = repository.hentUnikeEnhetnrs()
        log.info { "enheter med bestillinger som potensielt må anmodes: $hmsEnheter" }

        val rapporter = hmsEnheter.map { enhetnr ->
            val delerSomMangletDekningVedInnsending = repository.hentDelerTilRapportering(enhetnr)
            log.info { "deler som manglet dekning ved innsending for enhet $enhetnr: $delerSomMangletDekningVedInnsending" }

            val lagerstatuser = oebs.hentLagerstatusForEnhetnr(
                enhetnr = enhetnr,
                hmsnrs = delerSomMangletDekningVedInnsending.map { it.hmsnr }
            ).associateBy { it.artikkelnummer }

            // Sjekk om delene fremdeles må anmodes. Lagerstatus kan ha endret seg siden innsending.
            val delerSomFremdelesMåAnmodes = delerSomMangletDekningVedInnsending.map { del ->
                val lagerstatus = requireNotNull(lagerstatuser[del.hmsnr])
                beregnAnmodningsbehovForDel(del, lagerstatus)
            }.filter { it.antallSomMåAnmodes > 0 }


            val rapport = Anmodningrapport(enhetnr, delerSomFremdelesMåAnmodes)
            log.info { "Anmodingrapport for enhet $enhetnr: $rapport" }
            if (rapport.anmodningsbehov.isNotEmpty()) {
                slackClient.varsleOmAnmodningrapportSomMåSendesTilEnhet(rapport)
            }

            rapport
        }

        return rapporter
    }

    fun markerDelerSomRapportert(enhetnr: String) {
        repository.markerDelerSomRapportert(enhetnr)
    }

    fun markerDelerSomIkkeRapportert() {
        repository.markerDelerSomIkkeRapportert()
    }
}
