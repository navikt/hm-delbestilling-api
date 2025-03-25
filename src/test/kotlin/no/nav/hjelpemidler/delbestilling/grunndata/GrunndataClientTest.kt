package no.nav.hjelpemidler.delbestilling.grunndata

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hmsnrHjmTilHmsnrDeler
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hmsnrTilDel
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hmsnrTilHjelpemiddel
import org.junit.jupiter.api.Test
import kotlin.test.Ignore

fun main() = runBlocking {
    val grunndataClient = GrunndataClient(baseUrl = "https://finnhjelpemiddel.nav.no")
    val hovedhjelpemidler = hmsnrTilHjelpemiddel
    val harAlleDeler = mutableListOf<String>()
    hovedhjelpemidler.onEachIndexed { index, it ->
        val hmsnr = it.key
        val progress = "(${index+1}/${hovedhjelpemidler.size})"

        val grunndataHjelpemiddel = grunndataClient.hentHjelpemiddel(hmsnr).produkt
        if (grunndataHjelpemiddel == null) {
            println("$progress $hmsnr finnes ikke i grunndata, hopper over")
        } else {
            val manuelleDeler = hmsnrHjmTilHmsnrDeler[hmsnr]?.toList() ?: emptyList()
            val grunndataDeler = grunndataClient.hentDeler(seriesId = grunndataHjelpemiddel.seriesId, produktId = grunndataHjelpemiddel.id).produkter.map{ it.hmsArtNr }.toSet()
            if (grunndataDeler.containsAll(manuelleDeler)) {
                println("$progress $hmsnr har alle deler i grunndata som i manuell liste")
                harAlleDeler.add(hmsnr)
            } else {
                println("$progress $hmsnr har disse delene i manuell liste: $manuelleDeler")
                println("$progress $hmsnr har disse delene i grunndata: $grunndataDeler")
            }
        }
    }

    println("Helt ferdig! Følgende hmsnr har alle tilsvarende deler både i manuell liste og grunndata: $harAlleDeler")
}

