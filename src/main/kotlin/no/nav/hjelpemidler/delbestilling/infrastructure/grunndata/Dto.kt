package no.nav.hjelpemidler.delbestilling.infrastructure.grunndata

import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import java.util.UUID

data class ProduktResponse(
    val hits: Hits,
) {
    val produkter: List<GrunndataProdukt> = hits.hits.map { it._source }
    val produkt: GrunndataProdukt? = produkter.firstOrNull()
}

data class Hits(
    val hits: List<ProduktSource>
)

data class ProduktSource(
    val _source: GrunndataProdukt
)

data class GrunndataProdukt(
    val id: UUID,
    val title: String,
    val articleName: String,
    val seriesId: UUID,
    val hmsArtNr: Hmsnr?,
    val supplierRef: String,
    val attributes: Attributes,
    val isoCategory: String,
    val supplier: Supplier,
    val media: List<Media>,
    val accessory: Boolean,
    val sparePart: Boolean,
    val main: Boolean,
)

data class Attributes(
    val compatibleWith: CompatibleWith? = null,
)

data class CompatibleWith(
    val seriesIds: List<UUID>? = null,
    val productIds: List<UUID>? = null,
)

data class Media(
    val uri: String,
    val priority: Int,
    val type: String,
    val text: String,
    val source: String,
)

data class Supplier(
    val name: String,
    val id: UUID,
)