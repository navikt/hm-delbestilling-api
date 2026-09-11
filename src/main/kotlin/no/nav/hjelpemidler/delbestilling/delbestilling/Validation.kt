package no.nav.hjelpemidler.delbestilling.delbestilling

import no.nav.hjelpemidler.delbestilling.common.DelLinje
import no.nav.hjelpemidler.delbestilling.common.Delbestilling
import no.nav.hjelpemidler.delbestilling.common.DellinjeUkjentDel
import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.common.Serienr
import no.nav.hjelpemidler.delbestilling.oppslag.OppslagDelerRequest
import no.nav.hjelpemidler.delbestilling.oppslag.OppslagRequest

fun validateOppslagRequest(req: OppslagRequest) = listOf(
    validateHmsnr(req.hmsnr),
    validateSerienr(req.serienr)
).flatten()

fun validateOppslagDelerRequest(req: OppslagDelerRequest) = listOf(
    validateKunEntenSerienrEllerBrukernr(req.serienr, req.brukernr),
    validateSerienrEllerBrukernr(serienr = req.serienr, brukernr = req.brukernr)
).flatten()

fun validateDelbestillingRequest(req: DelbestillingRequest): List<String> = listOf(
    validateHmsnr(req.delbestilling.hmsnr),
    validateKunEntenSerienrEllerBrukernr(req.delbestilling.serienr, req.delbestilling.brukernr),
    validateSerienrEllerBrukernr(req.delbestilling.serienr, req.delbestilling.brukernr),
    validateOpplæringBatteri(req.delbestilling),
    listOfNotNull(
        if (req.delbestilling.deler.isEmpty() && req.delbestilling.ukjenteDeler.isEmpty()) "Delbestillingen må inneholde minst én dellinje" else null
    ),
    validateDeler(req.delbestilling.deler),
    validateUkjenteDeler(req.delbestilling.ukjenteDeler, req.delbestilling.epostTekniker),
).flatten()

fun validateDeler(deler: List<DelLinje>) = deler.mapNotNull { del ->
    if (del.antall < 1) "Kan ikke ha antall < 1. Fant antall=${del.antall} for hmsnr ${del.del.hmsnr}" else null
}

fun validateUkjenteDeler(ukjenteDeler: List<DellinjeUkjentDel>, epostTekniker: String?): List<String> {
    if (ukjenteDeler.isEmpty()) return emptyList()

    return listOfNotNull(
        if (epostTekniker?.trim()?.matches(EPOST_REGEX) != true) "Tekniker må oppgi en gyldig e-postadresse" else null,
    ) + ukjenteDeler.flatMap(::validateUkjentDel)
}

fun validateUkjentDel(dellinje: DellinjeUkjentDel): List<String> {
    val del = dellinje.delUkjent
    val hmsnr = del.hmsnr?.takeIf { it.isNotBlank() }
    val levArtNr = del.levArtNr?.takeIf { it.isNotBlank() }

    return listOfNotNull(
        if (dellinje.antall < 1) "Antall for ukjent del må være minst 1" else null,
        if (del.hmsnr == null && del.levArtNr == null) "Ukjent del må ha HMS-nr eller leverandørens artikkelnummer" else null,
        if (del.hmsnr != null && (hmsnr == null || validateHmsnr(hmsnr).isNotEmpty())) "HMS-nr for ukjent del må ha 6 siffer" else null,
        if (del.levArtNr != null && (levArtNr == null || levArtNr.length > 20)) "Leverandørens artikkelnummer må være 1-20 tegn" else null,
        if (levArtNr != null && del.beskrivelse.isNullOrBlank()) "Ukjent del med leverandørens artikkelnummer må ha en beskrivelse" else null,
        if (del.beskrivelse != null && del.beskrivelse.length > 200) "Beskrivelse av ukjent del kan ikke være lengre enn 200 tegn" else null,
    )
}

fun validateHmsnr(hmsnr: Hmsnr) = listOfNotNull(
    if (hmsnr.length != 6) "Hmsnr må ha 6 siffer" else null,
    if (!hmsnr.allDigits()) "Hmsnr skal kun bestå av tall" else null,
)

fun validateSerienr(serienr: Serienr) = listOfNotNull(
    if (serienr.length != 6) "Serienr må ha 6 siffer" else null,
    if (!serienr.allDigits()) "Serienr skal kun bestå av tall" else null,
)

fun validateKunEntenSerienrEllerBrukernr(serienr: String?, brukernr: String?): List<String> =
    if (!serienr.isNullOrBlank() && !brukernr.isNullOrBlank()) {
        listOf("Kan ikke inneholde både serienr. og brukernr")
    } else {
        emptyList()
    }

fun validateSerienrEllerBrukernr(serienr: Serienr?, brukernr: String?) = listOfNotNull(
    if (serienr == null && brukernr == null) {
        "Brukernr eller serienr må være satt"
    } else if (serienr != null) {
        if (serienr.length != 6) "Serienr må ha 6 siffer"
        else if (!serienr.allDigits()) "Serienr skal kun bestå av tall" else null
    } else if (brukernr != null) {
        if (brukernr.length !in 5..8) "Brukernr må være 5-8 siffer"
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

private val EPOST_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")