package no.nav.hjelpemidler.delbestilling.infrastructure.oebs

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.common.Lager
import no.nav.hjelpemidler.delbestilling.common.DelbestillingSak
import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.common.Lagerstatus
import no.nav.hjelpemidler.domain.person.Fødselsnummer
import no.nav.hjelpemidler.logging.teamInfo

private val log = KotlinLogging.logger {}

class Oebs(
    private val client: OebsApiProxy,
    val finnLagerenhet: FinnLagerenhet,
) {
    suspend fun hentFnrLeietakerFraSerienr(artnr: String, serienr: String): String? {
        log.info { "Henter leietaker for utlån: artnr=$artnr, serienr=$serienr" }
        return client.hentUtlånPåArtnrOgSerienr(artnr, serienr).utlån?.fnr
    }

    suspend fun hentFnr(brukernr: String): String {
        log.info { "Henter fnr for brukernr" }
        log.teamInfo { "Henter fnr for brukernr=$brukernr" }
        return client.hentFnr(brukernr).value
    }

    suspend fun hentUtlånPåArtNrOgSerienr(artnr: String, serienr: String): UtlånMedSerienr? {
        log.info { "Henter utlån: artnr=$artnr, serienr=$serienr" }
        return client.hentUtlånPåArtnrOgSerienr(artnr, serienr).utlån
    }

    suspend fun hentUtlånPåArtNrOgBrukernr(artnr: String, brukernr: String): List<Utlån> {
        log.info { "Henter utlån: artnr=$artnr, brukernr=$brukernr" }
        return client.hentUtlånPåArtnrOgBrukernr(artnr, brukernr).utlån
    }

    suspend fun hentPersoninfo(fnr: String): List<OebsPersoninfo> {
        log.info { "Henter personinfo" }
        return client.hentPersoninfo(fnr)
    }

    suspend fun harBrukerpass(fnr: String): Boolean {
        log.info { "Sjekker om innlogget bruker har brukerpass" }
        return client.hentBrukerpassinfo(fnr).brukerpass
    }

    suspend fun hentUtlånPåArtnr(artnr: String): List<UtlånMedSerienr> {
        log.info { "Henter utlån for $artnr" }
        return client.hentUtlånPåArtnr(artnr)
    }

    // TODO endre all bruk til å bruke slik som hentLagerstatusForKommunenummerAsMap
    suspend fun hentLagerstatusForKommunenummer(kommunenummer: String, hmsnrs: List<String>): List<Lagerstatus> {
        log.info { "Henter lagerstatus for kommunenummer $kommunenummer for hmsnrs $hmsnrs" }
        val lagerenhet = finnLagerenhet(kommunenummer)
        return hentLagerstatusForEnhet(lagerenhet, hmsnrs)
    }

    suspend fun hentLagerstatusForKommunenummerAsMap(kommunenummer: String, hmsnrs: List<String>): Map<Hmsnr, Lagerstatus> {
        return hentLagerstatusForKommunenummer(kommunenummer, hmsnrs)
            .associateBy { it.artikkelnummer }
    }

    suspend fun hentLagerstatusForEnhet(lager: Lager, hmsnrs: List<String>): List<Lagerstatus> {
        log.info { "Henter lagerstatus for enhet $lager for hmsnrs $hmsnrs" }
        val response = client.hentLagerstatusForEnhetnr(lager.nummer, hmsnrs)
        return response.map { it.tilLagerstatus() }
    }

    fun byggOrdre(
        sak: DelbestillingSak,
        brukersFnr: Fødselsnummer,
        innsendernavn: String,
    ): Ordre {
        val artikler = sak.delbestilling.deler.map { Artikkel(it.del.hmsnr, it.antall) }
        val forsendelsesinfo = genererForsendelsesinfo(sak.delbestilling.levering, innsendernavn)
        return Ordre(
            brukersFnr = brukersFnr.value,
            saksnummer = sak.saksnummer.toString(),
            innsendernavn = innsendernavn,
            artikler = artikler,
            forsendelsesinfo = forsendelsesinfo,
        )
    }
}
