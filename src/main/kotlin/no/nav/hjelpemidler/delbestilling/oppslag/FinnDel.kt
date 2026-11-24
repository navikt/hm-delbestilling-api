package no.nav.hjelpemidler.delbestilling.oppslag

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.Grunndata
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.data.hmsnr2Hjm
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.data.hmsnrTilDel
import java.util.UUID

private val log = KotlinLogging.logger {}

// Slår opp en del som tekniker har oppgitt manuelt med HMS-nr eller lev.art.nr.
class FinnDel(
    private val grunndata: Grunndata,
) {

    suspend operator fun invoke(request: DelOppslagRequest): FinnDelResultat = when (request) {
        is DelOppslagRequest.MedHmsnr -> medHmsnr(request)
        is DelOppslagRequest.MedLevArtNr -> medLevArtNr(request)
    }

    private suspend fun medHmsnr(request: DelOppslagRequest.MedHmsnr): FinnDelResultat {
        val hmsnr = request.hmsnr
        log.info { "Henter delinfo for hmsnr $hmsnr" }

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

        log.info { "Fant ikke del for hmsnr=$hmsnr (hjelpemiddel hmsnr=${request.hjelpemiddelHmsnr})" }
        return FinnDelResultat.IkkeFunnet
    }

    private suspend fun medLevArtNr(request: DelOppslagRequest.MedLevArtNr): FinnDelResultat {
        val levArtNr = request.levArtNr
        log.info { "Henter delinfo for levArtNr $levArtNr" }

        val treff = try {
            grunndata.hentProdukterMedLevArtNr(levArtNr).filterNot { it.erHovedprodukt }
        } catch (e: Exception) {
            log.info(e) { "Klarte ikke å søke etter levArtNr $levArtNr i grunndata" }
            emptyList()
        }

        val produkt = when (treff.size) {
            0, 1 -> treff.singleOrNull()
            else -> {
                // Lev.art.nr er bare unikt per leverandør, så vi snevrer inn til hjelpemiddelets leverandør.
                val leverandørId = hentLeverandørId(request.hjelpemiddelHmsnr)
                treff.filter { it.leverandørId == leverandørId }.singleOrNull()
            }
        }

        if (produkt == null) {
            log.info { "Fant ikke entydig del for levArtNr=$levArtNr (hjelpemiddel hmsnr=${request.hjelpemiddelHmsnr}). Antall treff i grunndata: ${treff.size}" }
            return FinnDelResultat.IkkeFunnet
        }

        return FinnDelResultat.Funnet(produkt.tilDel())
    }

    private suspend fun hentLeverandørId(hjelpemiddelHmsnr: Hmsnr): UUID? = try {
        grunndata.hentProdukt(hjelpemiddelHmsnr)?.leverandørId
    } catch (e: Exception) {
        log.info(e) { "Klarte ikke å hente leverandør for hjelpemiddel $hjelpemiddelHmsnr fra grunndata" }
        null
    }
}

sealed class DelOppslagRequest {
    abstract val hjelpemiddelHmsnr: Hmsnr

    data class MedHmsnr(val hmsnr: Hmsnr, override val hjelpemiddelHmsnr: Hmsnr) : DelOppslagRequest()
    data class MedLevArtNr(val levArtNr: String, override val hjelpemiddelHmsnr: Hmsnr) : DelOppslagRequest()
}

sealed class FinnDelResultat {
    data class Funnet(val del: Del) : FinnDelResultat()
    data class ErHjelpemiddel(val hjelpemiddelnavn: String) : FinnDelResultat()
    object IkkeFunnet : FinnDelResultat()
}
