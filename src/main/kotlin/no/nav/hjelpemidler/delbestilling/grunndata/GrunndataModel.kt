package no.nav.hjelpemidler.delbestilling.grunndata

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
    val articleName: String,
    val seriesId: UUID,
    val hmsArtNr: Hmsnr,
    val supplierRef: String,
)