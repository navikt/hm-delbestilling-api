package no.nav.hjelpemidler.delbestilling.infrastructure.grunndata

import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.data.hmsnrTilDel
import java.util.UUID

data class ProduktResponse(
    val hits: Hits,
) {
    val produkter: List<Produkt> = hits.hits.map { it._source }
    val produkt: Produkt? = produkter.firstOrNull()
}

data class Hits(
    val hits: List<ProduktSource>
)

data class ProduktSource(
    val _source: Produkt
)

data class Produkt(
    val id: UUID, // ProductId
    val title: String,
    val articleName: String,
    val seriesId: UUID,
    val hmsArtNr: Hmsnr,
    val supplierRef: String,
    val attributes: Attributes,
    val isoCategory: String,
    val supplier: Supplier,
    val media: List<Media>,
    val accessory: Boolean,
    val sparePart: Boolean,
    val main: Boolean,
) {
    fun bildeUrls(hmsnr: String): List<String> {
        val grunndataBildeUrls = media.filter { it.type == "IMAGE" }
            .sortedBy { it.priority }
            .map { "https://finnhjelpemiddel.nav.no/imageproxy/400d/${it.uri}" }

        if (grunndataBildeUrls.isNotEmpty()) {
            return grunndataBildeUrls
        }

        // Prøv fallback til bilde fra manuell liste
        val manuellDel = hmsnrTilDel[hmsnr]
        return manuellDel?.imgs ?: emptyList()
    }
}

data class Media(
    val uri: String,
    val priority: Int,
    val type: String, // TODO: heller bruk enum
    val text: String,
    val source: String,
)

data class Attributes(
    val compatibleWith: CompatibleWith? = null,
    val egnetForKommunalTekniker: Boolean? = null,
)

data class CompatibleWith(
    val seriesIds: List<UUID>? = null,
    val productIds: List<UUID>? = null,
)

data class Supplier(
    val name: String,
    val id: UUID,
)