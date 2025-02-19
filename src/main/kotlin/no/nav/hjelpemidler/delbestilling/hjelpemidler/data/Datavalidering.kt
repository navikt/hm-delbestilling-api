package no.nav.hjelpemidler.delbestilling.hjelpemidler.data

import no.nav.hjelpemidler.delbestilling.delbestilling.Del
import no.nav.hjelpemidler.delbestilling.delbestilling.Hmsnr
import no.nav.hjelpemidler.delbestilling.hjelpemidler.Hjelpemiddel

fun validerData() {
    val deler = hmsnrTilDel.values.toList()
    val hjelpemiddel = hmsnrTilHjelpemiddel.values.toList()
    kontrollerForDuplikateDeler(deler)
    kontrollerForDuplikateHjm(hjelpemiddel)
    kontrollerAtAlleBrukteHmsnrErDefinert(deler.map { it.hmsnr }, hjelpemiddel.map { it.hmsnr })
}

private fun kontrollerForDuplikateDeler(deler: List<Del>) {
    val duplicates = deler
        .groupBy { it }
        .filter { it.value.size > 1 }
        .flatMap { it.value }

    if (duplicates.isNotEmpty()) {
        throw IllegalStateException("DELER inneholder duplikate hmsnr: $duplicates")
    }
}

private fun kontrollerForDuplikateHjm(hjelpemidler: List<Hjelpemiddel>) {
    val duplicates = hjelpemidler
        .groupBy { it }
        .filter { it.value.size > 1 }
        .flatMap { it.value }

    if (duplicates.isNotEmpty()) {
        throw IllegalStateException("HJELPEMIDLER inneholder duplikate hmsnr: $duplicates")
    }
}

private fun kontrollerAtAlleBrukteHmsnrErDefinert(deler: List<Hmsnr>, hjelpemiddel: List<Hmsnr>) {
    deler.forEach { hmsnr -> requireNotNull(hmsnrTilHjelpemiddel[hmsnr]) { "Hjelpemiddel $hmsnr er ikke definert." } }
    hjelpemiddel.forEach { hmsnr -> requireNotNull(hmsnrTilDel[hmsnr]) { "Del $hmsnr er ikke definert." } }
}