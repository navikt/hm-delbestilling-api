package no.nav.hjelpemidler.delbestilling.grunndata

import kotlinx.coroutines.runBlocking
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hmsnrTilDel
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.GrunndataClient

fun main() = runBlocking {
    finnHjelpemidlerIManuellListeSomErDekketAvGrunndata()
}

private suspend fun finnHjelpemidlerIManuellListeSomErDekketAvGrunndata() {
    val client = GrunndataClient(baseUrl = "https://finnhjelpemiddel.nav.no")
    val deler = hmsnrTilDel.keys

    val delerSomMåSjekkesManuelt = mutableListOf<String>()

    deler.forEachIndexed { i, hmsnr ->
        println("Henter del nr $i")
        if (client.hentProdukt(hmsnr).produkt == null) {
            delerSomMåSjekkesManuelt.add(hmsnr)
        }
    }

    println("Antall manuell deler: ${deler.size} ")
    println("Deler hvor vi må finne leverandør manuelt: ${delerSomMåSjekkesManuelt.joinToString("\n") { "$it ${hmsnrTilDel[it]!!.navn}" }}")
}