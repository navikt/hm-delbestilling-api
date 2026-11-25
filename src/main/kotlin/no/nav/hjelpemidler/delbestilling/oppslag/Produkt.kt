package no.nav.hjelpemidler.delbestilling.oppslag

import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.common.Kilde
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.defaultAntall
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.maksAntall
import java.util.UUID

data class Produkt(
    val produktId: UUID,
    val tittel: String,
    val artikkelnavn: String,
    val serieId: UUID,
    val hmsArtNr: Hmsnr,
    val leverandørRef: String,
    val leverandørnavn: String,
    val leverandørId: UUID,
    val kompatibleProduktIder: List<UUID>,
    val kompatibleSerieIder: List<UUID>,
    val isoKategori: String,
    val bilder: List<String>,
    val erTilbehør: Boolean, // TODO endre til en enum type: TILBEHØR, RESERVEDEL, HJELPEMIDDEL
    val erReservedel: Boolean,
    val erHovedprodukt: Boolean,
)

fun Produkt.tilDel(): Del {
    val kategori = artikkelnavn.split(" ").first()
    return Del(
        hmsnr = hmsArtNr,
        navn = artikkelnavn,
        levArtNr = leverandørRef,
        kategori = kategori,
        maksAntall = maksAntall(kategori, isoKategori),
        kilde = Kilde.GRUNNDATA,
        defaultAntall = defaultAntall(kategori),
        imgs = bilder,
        erReservedel = erReservedel,
        erTilbehør = erTilbehør,
    )
}
