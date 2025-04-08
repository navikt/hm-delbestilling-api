package no.nav.hjelpemidler.delbestilling.delbestilling.anmodning

import com.microsoft.graph.models.BodyType
import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.delbestilling.delbestilling.model.DelbestillingSak
import no.nav.hjelpemidler.delbestilling.infrastructure.email.Email
import no.nav.hjelpemidler.delbestilling.infrastructure.email.enhetTilEpostadresse
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.Grunndata
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Oebs
import no.nav.hjelpemidler.delbestilling.isProd
import no.nav.hjelpemidler.delbestilling.slack.SlackClient
import no.nav.hjelpemidler.hjelpemidlerdigitalSoknadapi.tjenester.norg.NorgService

private val log = KotlinLogging.logger {}

class AnmodningService(
    private val repository: AnmodningRepository,
    private val oebs: Oebs,
    private val norgService: NorgService,
    private val slackClient: SlackClient,
    private val email: Email,
    private val grunndata: Grunndata,
) {

    suspend fun lagreDelerUtenDekning(sak: DelbestillingSak, tx: JdbcOperations) {
        val delerUtenDekning = finnDelerUtenDekning(sak)
        val enhet = norgService.hentHmsEnhet(sak.brukersKommunenummer)

        log.info { "Dekningsjekk: lagrer følgende deler uten dekning: ${delerUtenDekning.joinToString("\n")}" }

        if (delerUtenDekning.isNotEmpty()) {
            delerUtenDekning.forEach { del ->
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

            slackClient.rapporterOmDelerUtenDekning(delerUtenDekning, sak.brukersKommunenavn, enhet.enhetNr)
        }
    }

    fun finnDelerUtenDekning(sak: DelbestillingSak): List<AnmodningsbehovForDel> {
        val delerUtenDekning = sak.delbestilling.deler.map { dellinje ->
            val lagerstatus = requireNotNull(dellinje.lagerstatusPåBestillingstidspunkt)
            beregnAnmodningsbehovForDel(dellinje, lagerstatus)
        }.filter { it.antallSomMåAnmodes > 0 }

        return delerUtenDekning
    }

    suspend fun genererAnmodningsrapporter(): List<Anmodningrapport> {
        log.info { "Genererer rapport for bestilte deler som må anmodes" }

        // Hent først alle unike enhetnr
        val hmsEnheter = repository.hentUnikeEnhetnrs()
        log.info { "Enheter med deler som potensielt må anmodes: $hmsEnheter" }

        val rapporter = hmsEnheter.map { enhetnr ->
            val delerSomMangletDekningVedInnsending = repository.hentDelerTilRapportering(enhetnr)
            log.info { "Deler som manglet dekning ved innsending for enhet $enhetnr: $delerSomMangletDekningVedInnsending" }

            val lagerstatuser = oebs.hentLagerstatusForEnhetnr(
                enhetnr = enhetnr,
                hmsnrs = delerSomMangletDekningVedInnsending.map { it.hmsnr }
            ).associateBy { it.artikkelnummer }

            // Sjekk om delene fremdeles må anmodes. Lagerstatus kan ha endret seg siden innsending.
            val delerSomFremdelesMåAnmodes = delerSomMangletDekningVedInnsending.map { del ->
                val lagerstatus = requireNotNull(lagerstatuser[del.hmsnr])
                beregnAnmodningsbehovForDel(del, lagerstatus)
            }.filter { it.antallSomMåAnmodes > 0 }

            val rapport = Anmodningrapport(enhetnr = enhetnr, anmodningsbehov = delerSomFremdelesMåAnmodes)
            
            // Berik med leverandørnavn
            rapport.anmodningsbehov.forEach { behov ->
                val leverandørnavn =
                    grunndata.hentProdukt(behov.hmsnr)?.supplier?.name ?: manuelleLeverandørnavn[behov.hmsnr]
                    ?: "Ukjent"
                behov.leverandørnavn = leverandørnavn
            }

            log.info { "Anmodingrapport for enhet $enhetnr: $rapport" }

            rapport
        }

        return rapporter
    }


    fun markerDelerSomIkkeRapportert() {
        repository.markerDelerSomIkkeRapportert()
    }

    suspend fun sendAnmodningRapport(rapport: Anmodningrapport): String {
        // TODO: fjern denne
        if (isProd()) {
            log.info { "rapport før exception kastes: $rapport" }
            throw RuntimeException("Prøver å sende en anmodningsrapport på mail, stopper her")
        }

        val melding = rapportTilMelding(rapport)

        repository.withTransaction { tx ->
            repository.markerDelerSomRapportert(tx, rapport.enhetnr)
            repository.lagreAnmodninger(tx, rapport)
            email.sendSimpleMessage(
                to = enhetTilEpostadresse(rapport.enhetnr),
                subject = "Deler som må anmodes",
                contentType = BodyType.TEXT,
                content = melding
            )
        }

        return melding
    }

    suspend fun sendTestMail() {
        email.sendSimpleMessage(
            to = "hakonhagen2@gmail.com",
            subject = "TESTMAIL FRA DIGIHOT",
            contentType = BodyType.TEXT,
            content = "Hei på deg."
        )
    }
}
