package no.nav.hjelpemidler.delbestilling.delbestilling

import no.nav.hjelpemidler.delbestilling.common.DelLinje
import no.nav.hjelpemidler.delbestilling.common.Delbestilling
import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.common.Serienr
import no.nav.hjelpemidler.delbestilling.oppslag.OppslagDelerRequest
import no.nav.hjelpemidler.delbestilling.oppslag.OppslagRequest

fun validateOppslagRequest(req: OppslagRequest) = listOf(
    validateHmsnr(req.hmsnr),
    validateSerienr(req.serienr)
).flatten()

fun validateOppslagDelerRequest(req: OppslagDelerRequest) = listOf(
    validateSerienrEllerBrukernr(serienr = req.serienr, brukernr = req.brukernr)
).flatten()

fun validateDelbestillingRequest(req: DelbestillingRequest): List<String> = listOf(
    validateHmsnr(req.delbestilling.hmsnr),
    validateSerienrEllerBrukernr(req.delbestilling.serienr, req.delbestilling.brukernr),
    validateOpplæringBatteri(req.delbestilling),
    listOfNotNull(
        if (req.delbestilling.deler.isEmpty()) "Delbestillingen må inneholde minst én dellinje" else null
    ),
    validateDeler(req.delbestilling.deler),
).flatten()

fun validateDeler(deler: List<DelLinje>) = deler.mapNotNull { del ->
    if (del.antall < 1) "Kan ikke ha antall < 1. Fant antall=${del.antall} for hmsnr ${del.del.hmsnr}" else null
}

fun validateHmsnr(hmsnr: Hmsnr) = listOfNotNull(
    if (hmsnr.length != 6) "Hmsnr må ha 6 siffer" else null,
    if (!hmsnr.allDigits()) "Hmsnr skal kun bestå av tall" else null,
)

fun validateSerienr(serienr: Serienr) = listOfNotNull(
    if (serienr.length != 6) "Serienr må ha 6 siffer" else null,
    if (!serienr.allDigits()) "Serienr skal kun bestå av tall" else null,
)

fun validateSerienrEllerBrukernr(serienr: Serienr?, brukernr: String?) = listOfNotNull(
    if (serienr == null && brukernr == null) {
        "Brukernr eller serienr må være satt"
    } else if (serienr != null) {
        if (serienr.length != 6) "Serienr må ha 6 siffer"
        else if (!serienr.allDigits()) "Serienr skal kun bestå av tall" else null
    } else if (brukernr != null) {
        if (brukernr.length !in 6..8) "Brukernr må være 6-8 siffer"
        else if (!brukernr.allDigits()) "Brukernr skal kun bestå av tall" else null
    } else null
)

fun validateOpplæringBatteri(delbestilling: Delbestilling) = listOfNotNull(
    if (delbestilling.harBatteri() && delbestilling.harOpplæringPåBatteri != true) {
        "Tekniker må bekrefte opplæring i bytting av batteriene"
    } else null
)

fun requireHmsnr(value: String?): String {
    requireNotNull(value)
    requireNoErrors { validateHmsnr(value) }
    return value
}

fun requireSerienr(value: String?): String {
    requireNotNull(value)
    requireNoErrors { validateSerienr(value) }
    return value
}

fun requireNoErrors(validate: () -> List<String>) {
    validate().firstOrNull()?.let { throw IllegalArgumentException(it) }
}

private fun String.allDigits() = this.all { it.isDigit() }