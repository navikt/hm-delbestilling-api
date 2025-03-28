package no.nav.hjelpemidler.delbestilling.delbestilling

fun validateOppslagRequest(req: OppslagRequest) = listOf(
    validateHmsnr(req.hmsnr),
    validateSerienr(req.serienr)
).flatten()

fun validateDelbestillingRequest(req: DelbestillingRequest): List<String> = listOf(
    validateHmsnr(req.delbestilling.hmsnr),
    validateSerienr(req.delbestilling.serienr),
    validateOpplæringBatteri(req.delbestilling),
    listOfNotNull(
        if (req.delbestilling.deler.isEmpty()) "Delbestillingen må inneholde minst én dellinje" else null
    ),
).flatten()

fun validateHmsnr(hmsnr: Hmsnr) = listOfNotNull(
    if (hmsnr.length != 6) "Hmsnr må ha 6 siffer" else null,
    if (!hmsnr.allDigits()) "Hmsnr skal kun bestå av tall" else null,
)

fun validateSerienr(serienr: Serienr) = listOfNotNull(
    if (serienr.length != 6) "Serienr må ha 6 siffer" else null,
    if (!serienr.allDigits()) "Serienr skal kun bestå av tall" else null,
)

fun validateOpplæringBatteri(delbestilling: Delbestilling) = listOfNotNull(
    if (delbestilling.deler.any { it.del.kategori == "Batteri" } && delbestilling.harOpplæringPåBatteri != true) {
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