package no.nav.hjelpemidler.delbestilling.oppslag

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.common.Kilde
import no.nav.hjelpemidler.delbestilling.common.Lagerstatus
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Utlån
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.defaultAntall
import java.time.LocalDate

private val log = KotlinLogging.logger { }

data class HjelpemiddeloversiktResponse(
    val titler: Set<String>
)

data class Hjelpemiddel(
    val navn: String,
    val hmsnr: String,
    val deler: List<Del>,
    val antallDagerSidenSistBatteribestilling: Int? = null,
    val antallÅrGaranti: Int? = null,
    val erInnenforGaranti: Boolean? = null,
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

    fun medGaranti(utlån: Utlån, nå: LocalDate): Hjelpemiddel {
        if (utlån.opprettetDato == null) {
            log.info { "Utlån for artnr ${utlån.artnr} og serienr ${utlån.serienr} mangler opprettetDato, returnerer hjelpemiddel ikke beriket med garanti" }
            return this
        }

        if (utlån.isokode == null) {
            log.info { "Utlån for artnr ${utlån.artnr} og serienr ${utlån.serienr} mangler isokode, returnerer hjelpemiddel ikke beriket med garanti" }
            return this
        }

        val garantiPeriodeStart = utlån.opprettetDato // I OeBS er opprettet dato det samme som garantiperiode-start
        val isokode = utlån.isokode.take(4)

        val antallÅrGaranti = when(isokode) {
            "1223" -> 3 // 1223 = Motordrevne rullestoler (ERS) har garantitid på 3 år
            else -> 2
        }

        val garantiPeriodeSlutt = garantiPeriodeStart.plusYears(antallÅrGaranti.toLong())
        val erInnenforGaranti = nå.isBefore(garantiPeriodeSlutt)

        return this.copy(erInnenforGaranti = erInnenforGaranti, antallÅrGaranti = antallÅrGaranti)
    }
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
