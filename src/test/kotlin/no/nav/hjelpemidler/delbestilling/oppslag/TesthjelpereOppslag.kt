package no.nav.hjelpemidler.delbestilling.oppslag

import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.testdata.Testdata


fun del(
    hmsnr: Hmsnr = Testdata.defaultDelHmsnr,
    kategori: String = "Batteri",
) = Del(
    hmsnr = hmsnr,
    navn = hmsnr,
    kategori = kategori,
    maksAntall = 2
)

fun hjelpemiddel(
    deler: List<Del> = listOf(del()),
    hmsnr: Hmsnr = Testdata.defaultHjmHmsnr,
    navn: String = Testdata.defaultHjmNavn,
) = Hjelpemiddel(
    hmsnr = hmsnr,
    navn = navn,
    deler = deler
)