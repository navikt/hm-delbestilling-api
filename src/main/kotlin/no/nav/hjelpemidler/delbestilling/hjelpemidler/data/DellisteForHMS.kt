package no.nav.hjelpemidler.delbestilling.hjelpemidler.data

import no.nav.hjelpemidler.delbestilling.delbestilling.Hmsnr
import no.nav.hjelpemidler.delbestilling.hjelpemidler.Navn
import java.time.LocalDate


data class DellisteDel(
    val hmsnr: Hmsnr,
    val navn: Navn,
    val hjmNavn: Navn,
    val lagtTil: LocalDate,
)

data class Delliste(
    val deler: List<DellisteDel>,
    val sistOppdatert: LocalDate = maxOf(
        deler.maxOf { it.lagtTil },
        LocalDate.of(2024, 5, 31) // manuell overstyring pga fjerning av 251746 Eloflex
    )
)

val delliste = Delliste(
    hmsnrTilDelMedHjelpemiddel.values.map {
        DellisteDel(
            hmsnr = it.del.hmsnr,
            navn = it.del.navn,
            hjmNavn = it.hjelpemidler.first().navn,
            lagtTil = it.del.datoLagtTil ?: throw IllegalStateException("datoLagtTil mangler på ${it.del}")
        )
    }.sortedBy { it.hjmNavn }.sortedByDescending { it.lagtTil }
)
