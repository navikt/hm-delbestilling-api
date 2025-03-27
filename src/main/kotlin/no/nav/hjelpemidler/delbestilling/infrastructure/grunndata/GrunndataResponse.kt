package no.nav.hjelpemidler.delbestilling.infrastructure.grunndata

import no.nav.hjelpemidler.delbestilling.delbestilling.Hmsnr
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
    val media: List<Media>,
)

data class Media(
    val uri: String,
    val priority: Int,
    val type: MediaType,
    val text: String,
    val source: String
)

enum class MediaType {
    IMAGE
}

data class Attributes(
    val compatibleWith: CompatibleWith? = null,
)
data class CompatibleWith(
    val seriesIds: List<UUID>? = null,
    val productIds: List<UUID>? = null,
)