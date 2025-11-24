package no.nav.hjelpemidler.delbestilling.common

import no.nav.hjelpemidler.delbestilling.oppslag.legacy.defaultAntall
import no.nav.hjelpemidler.time.leggTilArbeidsdager
import no.nav.hjelpemidler.time.toInstant
import no.nav.hjelpemidler.time.toLocalDate
import java.time.LocalDate
import java.util.UUID

data class Delbestilling(
    val id: UUID,
    val hmsnr: Hmsnr,
    val serienr: Serienr,
    val deler: List<DelLinje>,
    val levering: Levering,
    val harOpplæringPåBatteri: Boolean?,
    val navn: String?,
    val status: Status = Status.INNSENDT,
) {
    fun oppdaterDellinjeStatus(status: DellinjeStatus, hmsnr: Hmsnr, datoOppdatert: LocalDate): Delbestilling {
        val oppdaterteDeler = deler.map {
            if (it.del.hmsnr == hmsnr) {
                it.copy(
                    status = status,
                    datoSkipningsbekreftet = datoOppdatert,
                    forventetLeveringsdato = beregnForventetLeveringsdato(datoOppdatert)
                )
            } else {
                it
            }
        }
        return copy(deler = oppdaterteDeler)
    }

    fun alleDelerErSkipningsbekreftet(): Boolean {
        return deler.all { it.status == DellinjeStatus.SKIPNINGSBEKREFTET }
    }

    fun harBatteri() = deler.any { it.erBatteri() }

    companion object {
        private const val LEVERINGSDAGER_FRA_SKIPNINGSBEKREFTELSE = 1

        private fun beregnForventetLeveringsdato(skipningsdato: LocalDate) = skipningsdato
            .toInstant()
            .leggTilArbeidsdager(LEVERINGSDAGER_FRA_SKIPNINGSBEKREFTELSE)
            .toLocalDate()
    }
}

data class DelLinje(
    val del: Del,
    val antall: Int,
    val status: DellinjeStatus? = null,
    val datoSkipningsbekreftet: LocalDate? = null,
    val forventetLeveringsdato: LocalDate? = null,
    val lagerstatusPåBestillingstidspunkt: Lagerstatus? = null
) {
    fun erBatteri() = del.erBatteri()
}


data class Del(
    val hmsnr: Hmsnr,
    val navn: String,
    val levArtNr: String? = null,
    val kategori: String,
    val defaultAntall: Int = defaultAntall(kategori),
    val maksAntall: Int,
    val imgs: List<String> = emptyList(),
    var lagerstatus: Lagerstatus? = null,
    val kilde: Kilde? = Kilde.MANUELL_LISTE,
) {
    fun erBatteri() = kategori == "Batteri"
}

data class Lagerstatus(
    val organisasjons_id: Int,
    val organisasjons_navn: String,
    val artikkelnummer: String,
    val minmax: Boolean,
    val tilgjengelig: Int,
    val antallDelerPåLager: Int,
)

enum class Kilde {
    GRUNNDATA,
    MANUELL_LISTE
}

typealias Hmsnr = String
typealias Serienr = String

enum class Levering {
    TIL_XK_LAGER, TIL_SERVICE_OPPDRAG,
}