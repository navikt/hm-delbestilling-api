package no.nav.hjelpemidler.delbestilling.oppslag

import no.nav.hjelpemidler.delbestilling.delbestilling.model.Hmsnr
import no.nav.hjelpemidler.delbestilling.delbestilling.model.Kilde
import no.nav.hjelpemidler.delbestilling.delbestilling.model.Lagerstatus
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.defaultAntall

data class HjelpemiddeloversiktResponse(
    val titler: Set<String>
)

data class Hjelpemiddel(
    val navn: String,
    val hmsnr: String,
    val deler: List<Del>,
) {
    val antallKategorier: Int = deler.distinctBy { it.kategori }.size

    fun delerHmsnr() = deler.map { it.hmsnr }

    fun sorterDeler(): Hjelpemiddel = this.copy(
        deler = deler.sortedBy { it.navn }
    )
}

fun Hjelpemiddel.medLagerstatus(lagerstatuser: Map<Hmsnr, Lagerstatus>): Hjelpemiddel =
    this.copy(
        deler = this.deler.map { del ->
            val lagerstatus = lagerstatuser[del.hmsnr]
                ?: throw IllegalStateException("Del ${del.hmsnr} på hjelpemiddel $hmsnr mangler lagerstatus, kan ikke returnere resultat")
            del.copy(lagerstatus = lagerstatus)
        }
    )

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
)

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
