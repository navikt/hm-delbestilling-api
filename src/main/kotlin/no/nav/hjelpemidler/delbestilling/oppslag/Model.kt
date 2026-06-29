package no.nav.hjelpemidler.delbestilling.oppslag

import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.common.Kilde
import no.nav.hjelpemidler.delbestilling.common.Lagerstatus
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.UtlånMedSerienr
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.defaultAntall

data class DelerTilHmsnrsRequest(
    val hmsnrs: List<Hmsnr>,
)

data class Hjelpemiddel(
    val navn: String,
    val hmsnr: String,
    val isoKode: String,
    val deler: List<Del>,
    val antallDagerSidenSistBatteribestilling: Int? = null,
    val antallÅrGaranti: Int? = null,
    val erInnenforGaranti: Boolean? = null,
) {
    val antallKategorier: Int = deler.distinctBy { it.kategori }.size

    fun delerHmsnr() = deler.map { it.hmsnr }

    fun sorterDeler(): Hjelpemiddel = this.copy(deler = deler.sortedBy { it.navn })

    fun harBatteri() = deler.any { it.erBatteri() }

    fun medAntallDagerSidenSistBatteribestilling(dager: Int?): Hjelpemiddel =
        this.copy(antallDagerSidenSistBatteribestilling = dager)

    fun berikMedGaranti(utlånMedSerienr: UtlånMedSerienr): Hjelpemiddel {
        val garanti = utlånMedSerienr.garanti() ?: return this
        return this.copy(erInnenforGaranti = garanti.erInnenforGaranti(), antallÅrGaranti = garanti.antallÅr)
    }
}

private val serienummerstyrteIso4koder = setOf("1222", "1223", "1236", "1830")
private val serienummerstyrteIso6koder = setOf("181204", "181207", "181210", "220318")

data class HjelpemiddelUtenDeler(
    val navn: String,
    val hmsnr: String,
    val isoKode: String
) {
    val erSerienrStyrt: Boolean =
        isoKode.take(4) in serienummerstyrteIso4koder || isoKode.take(6) in serienummerstyrteIso6koder
}

data class Del(
    val hmsnr: Hmsnr,
    val navn: String,
    val levArtNr: String? = null,
    val kategori: String,
    val defaultAntall: Int = defaultAntall(kategori),
    val maksAntall: Int,
    val imgs: List<String> = emptyList(),
    val lagerstatus: Lagerstatus? = null,
    val kilde: Kilde? = Kilde.MANUELL_LISTE,
    val erReservedel: Boolean = false,
    val erTilbehør: Boolean = false,
) {
    fun erBatteri(): Boolean = kategori == "Batteri"
}

data class OppslagRequest(
    val hmsnr: String,
    val serienr: String,
)

data class OppslagDelerRequest(
    val serienr: String?,
    val brukernr: String?,
)


data class OppslagResultat(
    val hjelpemiddel: Hjelpemiddel,
    val piloter: List<Pilot> = emptyList(),
)

data class OppslagsResultatUtenDeler(
    val hjelpemiddel: HjelpemiddelUtenDeler,
)

enum class Pilot {
    BESTILLE_IKKE_FASTE_LAGERVARER
}
