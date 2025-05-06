package no.nav.hjelpemidler.delbestilling.oppslag.legacy.data

import no.nav.hjelpemidler.delbestilling.delbestilling.model.Hmsnr
import no.nav.hjelpemidler.delbestilling.oppslag.Del
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.Hjelpemiddelnavn

fun main() = validerData() // For lokal kjøring

fun validerData() {
    val deler = hmsnrTilDel.values.toList()
    val hjelpemidler = hmsnrTilHjelpemiddelnavn.values.toList()
    kontrollerForDuplikateDeler(deler)
    kontrollerForDuplikateHjm(hjelpemidler)
    kontrollerAtAlleBrukteHmsnrErDefinert(deler.map { it.hmsnr }, hjelpemidler.map { it.hmsnr })
    kontrollerAtAlleDelerTilhørerMinstEttHjelpemiddel()
    kontrollerAtAlleHjelpemiddelHarMinstEnDel(hjelpemidler)

}

fun kontrollerAtAlleDelerTilhørerMinstEttHjelpemiddel() {
    hmsnrTilDelMedHjelpemiddel.forEach {
        require(it.value.hjelpemidler.isNotEmpty()) { "Del ${it.key} er ikke koblet til et hjelpemiddel" }
    }
}

fun kontrollerAtAlleHjelpemiddelHarMinstEnDel(hjelpemidler: List<Hjelpemiddelnavn>) {
    hjelpemidler.forEach { hjm ->
        require(hmsnrHjmTilHmsnrDeler[hjm.hmsnr]?.isNotEmpty() == true) {"Hjelpemiddel ${hjm.hmsnr} ${hjm.navn} har ingen deler."}
    }
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

private fun kontrollerForDuplikateHjm(hjelpemidler: List<Hjelpemiddelnavn>) {
    val duplicates = hjelpemidler
        .groupBy { it }
        .filter { it.value.size > 1 }
        .flatMap { it.value }

    if (duplicates.isNotEmpty()) {
        throw IllegalStateException("HJELPEMIDLER inneholder duplikate hmsnr: $duplicates")
    }
}

private fun kontrollerAtAlleBrukteHmsnrErDefinert(deler: List<Hmsnr>, hjelpemiddel: List<Hmsnr>) {
    deler.forEach { hmsnr -> requireNotNull(hmsnrTilDel[hmsnr]) { "Del $hmsnr er ikke definert." } }
    hjelpemiddel.forEach { hmsnr -> requireNotNull(hmsnrTilHjelpemiddelnavn[hmsnr]) { "Hjelpemiddel $hmsnr er ikke definert." } }
}