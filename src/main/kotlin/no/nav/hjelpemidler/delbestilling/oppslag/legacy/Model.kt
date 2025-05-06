package no.nav.hjelpemidler.delbestilling.oppslag.legacy

import no.nav.hjelpemidler.delbestilling.delbestilling.model.Hmsnr
import no.nav.hjelpemidler.delbestilling.oppslag.Del

typealias Navn = String

data class Hjelpemiddelnavn(
    val navn: Navn,
    val hmsnr: Hmsnr
)

data class DelMedHjelpemidler(
    val del: Del,
    val hjelpemidler: List<Hjelpemiddelnavn>
)