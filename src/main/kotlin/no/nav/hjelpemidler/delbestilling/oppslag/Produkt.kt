package no.nav.hjelpemidler.delbestilling.oppslag

import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import java.util.UUID

data class Produkt(
    val produktId: UUID,
    val tittel: String,
    val artikkelnavn: String,
    val serieId: UUID,
    val hmsArtNr: Hmsnr,
    val leverandørRef: String,
    val leverandørnavn: String,
    val kompatibleProduktIder: List<UUID>,
    val kompatibleSerieIder: List<UUID>,
    val isoKategori: String,
    val bilder: List<String>,
    val erTilbehør: Boolean, // TODO endre til en enum type: TILBEHØR, RESERVEDEL, HJELPEMIDDEL
    val erReservedel: Boolean,
    val erHovedprodukt: Boolean,
)
