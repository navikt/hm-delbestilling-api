package no.nav.hjelpemidler.delbestilling.oppslag

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.Grunndata
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.data.hmsnr2Hjm
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.data.hmsnrTilDel

private val log = KotlinLogging.logger {}

// Slår opp en del som tekniker har oppgitt manuelt med HMS-nr.
class FinnDel(
    private val grunndata: Grunndata,
) {

    suspend operator fun invoke(hmsnr: Hmsnr): FinnDelResultat {
        log.info { "Henter delinfo for $hmsnr" }

        val produkt = try {
            grunndata.hentProdukt(hmsnr)
        } catch (e: Exception) {
            log.info(e) { "Klarte ikke å sjekke $hmsnr i grunndata" }
            null
        }

        if (produkt != null) {
            return if (produkt.erHovedprodukt) {
                FinnDelResultat.ErHjelpemiddel(produkt.artikkelnavn)
            } else {
                FinnDelResultat.Funnet(produkt.tilDel())
            }
        }

        hmsnr2Hjm[hmsnr]?.let { return FinnDelResultat.ErHjelpemiddel(it.navn) }
        hmsnrTilDel[hmsnr]?.let { return FinnDelResultat.Funnet(it) }

        return FinnDelResultat.IkkeFunnet
    }
}

sealed class FinnDelResultat {
    data class Funnet(val del: Del) : FinnDelResultat()
    data class ErHjelpemiddel(val hjelpemiddelnavn: String) : FinnDelResultat()
    object IkkeFunnet : FinnDelResultat()
}
