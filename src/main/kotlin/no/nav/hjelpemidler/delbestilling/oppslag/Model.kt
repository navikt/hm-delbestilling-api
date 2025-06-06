package no.nav.hjelpemidler.delbestilling.oppslag

import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.common.Kilde
import no.nav.hjelpemidler.delbestilling.common.Lagerstatus
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.defaultAntall

data class HjelpemiddeloversiktResponse(
    val titler: Set<String>
)

data class Hjelpemiddel(
    val navn: String,
    val hmsnr: String,
    val deler: List<Del>,
    val antallDagerSidenSistBatteribestilling: Int? = null,
) {
    val antallKategorier: Int = deler.distinctBy { it.kategori }.size

    fun delerHmsnr() = deler.map { it.hmsnr }

    fun sorterDeler(): Hjelpemiddel = this.copy(deler = deler.sortedBy { it.navn })

    fun harBatteri() = deler.any { it.erBatteri() }

    fun medLagerstatus(lagerstatuser: Map<Hmsnr, Lagerstatus>): Hjelpemiddel =
        this.copy(
            deler = this.deler.map { del ->
                val lagerstatus = lagerstatuser[del.hmsnr]
                    ?: throw IllegalStateException("Del ${del.hmsnr} på hjelpemiddel $hmsnr mangler lagerstatus, kan ikke returnere resultat")
                del.copy(lagerstatus = lagerstatus)
            }
        )

    fun medAntallDagerSidenSistBatteribestilling(dager: Int?): Hjelpemiddel =
        this.copy(antallDagerSidenSistBatteribestilling = dager)
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
) {
    fun erBatteri(): Boolean = kategori == "Batteri"
}

data class OppslagRequest(
    val hmsnr: String,
    val serienr: String,
)

data class OppslagResultat(
    val hjelpemiddel: Hjelpemiddel,
    val piloter: List<Pilot> = emptyList(),
)

enum class Pilot {
    BESTILLE_IKKE_FASTE_LAGERVARER
}
