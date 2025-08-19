package no.nav.hjelpemidler.delbestilling.infrastructure.oebs

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.common.Enhet
import no.nav.hjelpemidler.delbestilling.common.DelbestillingSak
import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.common.Lagerstatus
import no.nav.hjelpemidler.domain.person.Fødselsnummer

private val log = KotlinLogging.logger {}

class Oebs(
    private val client: OebsApiProxy,
    private val oebsSink: OebsSink,
) {
    suspend fun hentFnrLeietaker(artnr: String, serienr: String): String? {
        log.info { "Henter leietaker for utlån: artnr=$artnr, serienr=$serienr" }
        return client.hentUtlånPåArtnrOgSerienr(artnr, serienr).utlån?.fnr
    }

    suspend fun hentUtlånPåArtNrOgSerienr(artnr: String, serienr: String): Utlån? {
        log.info { "Henter utlån: artnr=$artnr, serienr=$serienr" }
        return client.hentUtlånPåArtnrOgSerienr(artnr, serienr).utlån
    }

    suspend fun hentPersoninfo(fnr: String): List<OebsPersoninfo> {
        log.info { "Henter personinfo" }
        return client.hentPersoninfo(fnr)
    }

    suspend fun harBrukerpass(fnr: String): Boolean {
        log.info { "Sjekker om innlogget bruker har brukerpass" }
        return client.hentBrukerpassinfo(fnr).brukerpass
    }

    suspend fun hentUtlånPåArtnr(artnr: String): List<Utlån> {
        log.info { "Henter utlån for $artnr" }
        return client.hentUtlånPåArtnr(artnr)
    }

    // TODO endre all bruk til å bruke slik som hentLagerstatusForKommunenummerAsMap
    suspend fun hentLagerstatusForKommunenummer(kommunenummer: String, hmsnrs: List<String>): List<Lagerstatus> {
        log.info { "Henter lagerstatus for kommunenummer $kommunenummer for hmsnrs $hmsnrs" }
        val response = client.hentLagerstatusForKommunenummer(kommunenummer, hmsnrs)
        return response.map { it.tilLagerstatus() }
    }

    suspend fun hentLagerstatusForKommunenummerAsMap(kommunenummer: String, hmsnrs: List<String>): Map<Hmsnr, Lagerstatus> {
        return hentLagerstatusForKommunenummer(kommunenummer, hmsnrs)
            .associateBy { it.artikkelnummer }
    }

    suspend fun hentLagerstatusForEnhet(enhet: Enhet, hmsnrs: List<String>): List<Lagerstatus> {
        log.info { "Henter lagerstatus for enhet $enhet for hmsnrs $hmsnrs" }
        val response = client.hentLagerstatusForEnhetnr(enhet.nummer, hmsnrs)
        return response.map { it.tilLagerstatus() }
    }

    fun sendDelbestilling(
        sak: DelbestillingSak,
        brukersFnr: Fødselsnummer,
        innsendernavn: String,
    ) {
        log.info { "Sender delbestilling for saksnummer '${sak.saksnummer}'" }

        val artikler = sak.delbestilling.deler.map { Artikkel(it.del.hmsnr, it.antall) }
        val forsendelsesinfo = genererForsendelsesinfo(sak.delbestilling.levering, innsendernavn)

        return oebsSink.sendDelbestilling(
            Ordre(
                brukersFnr = brukersFnr.value,
                saksnummer = sak.saksnummer.toString(),
                innsendernavn = innsendernavn,
                artikler = artikler,
                forsendelsesinfo = forsendelsesinfo,
            )
        )
    }
}
