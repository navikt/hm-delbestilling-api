package no.nav.hjelpemidler.delbestilling.oppslag

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.Grunndata
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.data.hmsnr2Hjm

private val log = KotlinLogging.logger {}

// Slår kun opp navn/isoKode for hjelpemiddelet, uten å hente deler.
// Deler hentes senere av FinnDelerTilHjelpemiddel, når serienr/brukernr er fylt inn.
class FinnHjelpemiddel(
    private val grunndata: Grunndata,
) {

    suspend operator fun invoke(hmsnr: Hmsnr): FinnDelerResultat {
        log.info { "Henter hjelpemiddelinfo for $hmsnr" }

        val produkt = try {
            grunndata.hentProdukt(hmsnr)?.takeIf { it.erHovedprodukt }
        } catch (e: Exception) {
            log.info(e) { "Klarte ikke å sjekke $hmsnr i grunndata" }
            null
        }

        val hjelpemiddel = when {
            produkt != null -> Hjelpemiddel(
                navn = produkt.artikkelnavn,
                hmsnr = produkt.hmsArtNr,
                isoKode = produkt.isoKategori,
                deler = emptyList(),
            )
            else -> hmsnr2Hjm[hmsnr]?.copy(deler = emptyList())
        } ?: return FinnDelerResultat.IkkeFunnet(OppslagFeil.TILBYR_IKKE_HJELPEMIDDEL)

        log.info { "hjelpemiddelinfo for $hmsnr: $hjelpemiddel" }
        return FinnDelerResultat.Funnet(hjelpemiddel)
    }
}
