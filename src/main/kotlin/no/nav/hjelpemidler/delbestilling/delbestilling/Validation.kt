package no.nav.hjelpemidler.delbestilling.delbestilling

fun validateOppslagRequest(req: OppslagRequest) = listOf(
    validateHmsnr(req.hmsnr),
    validateSerienr(req.serienr)
).flatten()

fun validateDelbestillingRequest(req: DelbestillingRequest) = listOf(
    validateHmsnr(req.delbestilling.hmsnr),
    validateSerienr(req.delbestilling.serienr),
    req.delbestilling.deler.map { validateDelLinje(it) }.flatten(),
).flatten()

fun validateDelLinje(delLinje: DelLinje) = listOf(
    validateDel(delLinje.del)
).flatten()

fun validateDel(del: Del) = listOf(
    validateHmsnr(del.hmsnr),
).flatten()


fun validateHmsnr(hmsnr: Hmsnr) = listOfNotNull(
    if (hmsnr.length != 6) "Hmsnr må ha 6 siffer" else null,
    if (hmsnr.any { !it.isDigit() }) "Hmsnr skal kun bestå av tall" else null,
)


fun validateSerienr(serienr: Serienr) = listOfNotNull(
    if (serienr.length != 6) "Serienr må ha 6 siffer" else null,
    if (serienr.any { !it.isDigit() }) "Serienr skal kun bestå av tall" else null,
)
