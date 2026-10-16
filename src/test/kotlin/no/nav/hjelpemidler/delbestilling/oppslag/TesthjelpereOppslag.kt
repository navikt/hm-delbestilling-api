package no.nav.hjelpemidler.delbestilling.oppslag

import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.testdata.DEFAULT_DEL_HMSNR
import no.nav.hjelpemidler.delbestilling.testdata.DEFAULT_HJM_HMSNR
import no.nav.hjelpemidler.delbestilling.testdata.DEFAULT_HJM_NAVN
fun del(
    hmsnr: Hmsnr = DEFAULT_DEL_HMSNR,
    kategori: String = "Batteri",
) = Del(
    hmsnr = hmsnr,
    navn = hmsnr,
    kategori = kategori,
    maksAntall = 2
)

fun hjelpemiddel(
    deler: List<Del> = listOf(del()),
    hmsnr: Hmsnr = DEFAULT_HJM_HMSNR,
    navn: String = DEFAULT_HJM_NAVN,
    isoKode: String = "12220301",
) = Hjelpemiddel(
    hmsnr = hmsnr,
    navn = navn,
    deler = deler,
    isoKode = isoKode
)