package no.nav.hjelpemidler.delbestilling.oppslag.legacy

import no.nav.hjelpemidler.delbestilling.delbestilling.model.Del
import no.nav.hjelpemidler.delbestilling.delbestilling.model.Hmsnr

typealias Navn = String

data class Hjelpemiddel(
    val navn: Navn,
    val hmsnr: Hmsnr
)

data class DelMedHjelpemidler(
    val del: Del,
    val hjelpemidler: List<Hjelpemiddel>
)