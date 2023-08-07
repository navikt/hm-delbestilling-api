package no.nav.hjelpemidler.delbestilling.delbestilling

import no.nav.hjelpemidler.delbestilling.hjelpemidler.HjelpemiddelDeler

fun validateOppslagRequest(req: OppslagRequest) = listOf(
    validateHmsnr(req.hmsnr),
    validateSerienr(req.serienr)
).flatten()

fun validateDelbestillingRequest(req: DelbestillingRequest): List<String> = listOf(
    validateHmsnr(req.delbestilling.hmsnr),
    validateSerienr(req.delbestilling.serienr),
    req.delbestilling.deler.map { validateDelLinje(it) }.flatten(),
    listOfNotNull(
        if (req.delbestilling.deler.isEmpty()) "Delbestillingen må inneholde minst én dellinje" else null
    ),
).flatten()

fun validateDelLinje(delLinje: DelLinje): List<String> {
    val feilmeldinger = validateDel(delLinje.del).toMutableList()

    val hmsnr = delLinje.del.hmsnr
    if (hmsnr !in DELER_I_SORTIMENT.keys) {
        feilmeldinger.add("$hmsnr finnes ikke i sortimentet av deler")
    } else {
        val maksAntall = DELER_I_SORTIMENT[hmsnr]!!.maksAntall
        if (delLinje.antall > maksAntall) {
            feilmeldinger.add("${delLinje.antall} overskrider maks antall ($maksAntall) for $hmsnr")
        }
    }

    return feilmeldinger
}


fun validateDel(del: Del) = listOf(
    validateHmsnr(del.hmsnr),
    listOfNotNull(
        if (del.hmsnr !in DELER_I_SORTIMENT) "${del.hmsnr} finnes ikke i sortimentet av deler" else null
    )
).flatten()


fun validateHmsnr(hmsnr: Hmsnr) = listOfNotNull(
    if (hmsnr.length != 6) "Hmsnr må ha 6 siffer" else null,
    if (hmsnr.any { !it.isDigit() }) "Hmsnr skal kun bestå av tall" else null,
)


fun validateSerienr(serienr: Serienr) = listOfNotNull(
    if (serienr.length != 6) "Serienr må ha 6 siffer" else null,
    if (serienr.any { !it.isDigit() }) "Serienr skal kun bestå av tall" else null,
)

val DELER_I_SORTIMENT = HjelpemiddelDeler.hentAlleHjelpemidlerMedDeler()
    .mapNotNull { it.deler }
    .flatten()
    .associateBy { it.hmsnr }