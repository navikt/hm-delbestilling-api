package no.nav.hjelpemidler.delbestilling.infrastructure.grunndata

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.oppslag.Produkt
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.data.hmsnrTilDel
import java.util.UUID

private val log = KotlinLogging.logger {}

class Grunndata(private val client: GrunndataClientInterface) {

    suspend fun hentProdukt(hmsnr: Hmsnr): Produkt? {
        log.info { "Henter produkt $hmsnr fra grunndata" }
        val produkt = client.hentProdukt(hmsnr).produkt?.toDomain()
        log.info { "Produkt for $hmsnr: $produkt" }
        return produkt
    }

    suspend fun hentDeler(seriesId: UUID, produktId: UUID): List<Produkt> {
        log.info { "Henter deler for seriesId $seriesId og produktId $produktId fra grunndata" }
        return client.hentDeler(seriesId = seriesId, produktId = produktId).produkter.mapNotNull { it.toDomain() }
    }

    suspend fun hentAlleDelerSomKanBestilles(): List<Produkt> {
        log.info { "Henter alle deler som kan bestilles fra grunndata" }
        return client.hentAlleDelerSomKanBestilles().produkter.mapNotNull { it.toDomain() }
    }

    suspend fun hentAlleHjmMedIdEllerSeriesId(seriesIds: Set<UUID>, produktIds: Set<UUID>): List<Produkt> {
        log.info { "Henter alle hjm med gitte id eller seriesId fra grunndata" }
        return client.hentAlleHjmMedIdEllerSeriesId(seriesIds = seriesIds, produktIds = produktIds).produkter.mapNotNull { it.toDomain() }
    }
}

private fun GrunndataProdukt.toDomain(): Produkt? {
    val hmsnr = hmsArtNr ?: return null
    return Produkt(
        produktId = id,
        tittel = title,
        artikkelnavn = articleName,
        serieId = seriesId,
        hmsArtNr = hmsnr,
        leverandørRef = supplierRef,
        leverandørnavn = supplier.name,
        kompatibleProduktIder = attributes.compatibleWith?.productIds ?: emptyList(),
        kompatibleSerieIder = attributes.compatibleWith?.seriesIds ?: emptyList(),
        isoKategori = isoCategory,
        bilder = beregnBildeUrls(hmsnr),
        erTilbehør = accessory,
        erReservedel = sparePart,
        erHovedprodukt = main,
    )
}

private fun GrunndataProdukt.beregnBildeUrls(hmsnr: Hmsnr): List<String> {
    val fraGrunndata = media
        .filter { it.type == "IMAGE" }
        .sortedBy { it.priority }
        .map { "https://finnhjelpemiddel.nav.no/imageproxy/400d/${it.uri}" }

    if (fraGrunndata.isNotEmpty()) return fraGrunndata

    return hmsnrTilDel[hmsnr]?.imgs ?: emptyList()
}