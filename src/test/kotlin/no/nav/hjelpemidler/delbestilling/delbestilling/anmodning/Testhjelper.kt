package no.nav.hjelpemidler.delbestilling.delbestilling.anmodning

import no.nav.hjelpemidler.delbestilling.delbestilling.model.Hmsnr
import no.nav.hjelpemidler.delbestilling.delbestilling.model.Lagerstatus


fun del(
    antall: Int,
    hmsnr: Hmsnr = "123456"
) = Del(
    hmsnr = hmsnr,
    navn = hmsnr,
    antall = antall,
)

fun lagerstatus(
    antall: Int,
    hmsnr: Hmsnr = "123456",
    minmax: Boolean = false,
) = Lagerstatus(
    organisasjons_id = 4703,
    organisasjons_navn = "HMS Oslo",
    artikkelnummer = hmsnr,
    minmax = minmax,
    tilgjengelig = antall,
    antallDelerPåLager = antall,
)

