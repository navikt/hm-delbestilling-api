package no.nav.hjelpemidler.delbestilling.delbestilling.anmodning

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.common.DelbestillingSak
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.FinnLagerenhet
import no.nav.hjelpemidler.delbestilling.infrastructure.email.Email
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.Grunndata
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Oebs
import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction.Transactional
import no.nav.hjelpemidler.delbestilling.infrastructure.slack.Slack

private val log = KotlinLogging.logger {}

class AnmodningService(
    private val transaction: Transactional,
    private val oebs: Oebs,
    private val slack: Slack,
    private val email: Email,
    private val grunndata: Grunndata,
) {

    suspend fun lagreDelerUtenDekning(sak: DelbestillingSak) {
        val delerUtenDekning = finnDelerUtenDekning(sak)
        val enhet = oebs.finnLagerenhet(sak.brukersKommunenummer)

        log.info { "Dekningsjekk: lagrer følgende deler uten dekning: ${delerUtenDekning.joinToString("\n")}" }

        if (delerUtenDekning.isNotEmpty()) {
            transaction {
                delerUtenDekning.forEach { del ->
                    delUtenDekningDao.lagreDelerUtenDekning(
                        saksnummer = sak.saksnummer,
                        hmsnr = del.hmsnr,
                        navn = del.navn,
                        antallUtenDekning = del.antallSomMåAnmodes,
                        bukersKommunenummer = sak.brukersKommunenummer,
                        brukersKommunenavn = sak.brukersKommunenavn,
                        enhetnr = enhet.nummer,
                    )
                }
            }

            slack.varsleOmDelerUtenDekning(delerUtenDekning, sak.brukersKommunenavn, enhet.nummer)
        }
    }

    fun finnDelerUtenDekning(sak: DelbestillingSak): List<AnmodningsbehovForDel> {
        val delerUtenDekning = sak.delbestilling.deler.map { dellinje ->
            val lagerstatus = requireNotNull(dellinje.lagerstatusPåBestillingstidspunkt)
            beregnAnmodningsbehovForDelVedInnsending(
                Del(hmsnr = dellinje.del.hmsnr, navn = dellinje.del.navn, antall = dellinje.antall),
                lagerstatus
            )
        }.filter { it.antallSomMåAnmodes > 0 }

        return delerUtenDekning
    }

    suspend fun genererAnmodningsrapporter(): List<Anmodningrapport> {
        log.info { "Genererer rapport for bestilte deler som må anmodes" }

        // Hent først alle unike enhetnr
        val hmsEnheter = transaction { delUtenDekningDao.hentUnikeEnheter() }
        log.info { "Enheter med deler som potensielt må anmodes: $hmsEnheter" }

        val rapporter = hmsEnheter.map { enhet ->
            val delerSomMangletDekningVedInnsending = transaction{ delUtenDekningDao.hentDelerTilRapportering(enhet.nummer) }
            log.info { "Deler som manglet dekning ved innsending for enhet $enhet: $delerSomMangletDekningVedInnsending" }

            val lagerstatuser = oebs.hentLagerstatusForEnhet(
                lager = enhet,
                hmsnrs = delerSomMangletDekningVedInnsending.map { it.hmsnr }
            ).associateBy { it.artikkelnummer }

            log.info { "lagerstatuser for enhet $enhet:: $lagerstatuser" }

            // Sjekk om delene fremdeles må anmodes. Lagerstatus kan ha endret seg siden innsending.
            val delerSomFremdelesMåAnmodes = delerSomMangletDekningVedInnsending.map { del ->
                val lagerstatus = requireNotNull(lagerstatuser[del.hmsnr])
                beregnAnmodningsbehovVedRapportering(del, lagerstatus)
            }.filter { it.antallSomMåAnmodes > 0 }

            val rapport = Anmodningrapport(lager = enhet, anmodningsbehov = delerSomFremdelesMåAnmodes)

            // Berik med leverandørnavn
            rapport.anmodningsbehov.forEach { behov ->
                val leverandørnavn =
                    grunndata.hentProdukt(behov.hmsnr)?.supplier?.name ?: manuelleLeverandørnavn[behov.hmsnr]
                    ?: "Ukjent"
                behov.leverandørnavn = leverandørnavn
            }

            log.info { "Anmodingrapport for enhet $enhet: $rapport" }

            rapport
        }

        return rapporter
    }


    suspend fun markerDelerSomIkkeRapportert() = transaction {
        delUtenDekningDao.markerDelerSomIkkeRapportert()
    }

    suspend fun sendAnmodningRapport(rapport: Anmodningrapport): String {
        val melding = rapportTilMelding(rapport)

        transaction {
            delUtenDekningDao.markerDelerSomRapportert(rapport.lager)
            anmodningDao.lagreAnmodninger(rapport)
            email.sendSimpleMessage(
                recipentEmail = rapport.lager.epost(),
                subject = "Deler som må anmodes",
                bodyText = melding
            )
        }

        return melding
    }
}
