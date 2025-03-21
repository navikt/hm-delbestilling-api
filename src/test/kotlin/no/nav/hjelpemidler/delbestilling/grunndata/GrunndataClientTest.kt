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
    hovedhjelpemidler.forEach{
        val hmsnr = it.key

        val grunndataHjelpemiddel = grunndataClient.hentHjelpemiddel(hmsnr).produkt
        if (grunndataHjelpemiddel == null) {
            println("$hmsnr finnes ikke i grunndata, hopper over")
        } else {
            val manuelleDeler = hmsnrHjmTilHmsnrDeler[hmsnr]?.toList() ?: emptyList()
            val grunndataDeler = grunndataClient.hentDeler(seriesId = grunndataHjelpemiddel.seriesId, produktId = grunndataHjelpemiddel.id).produkter.map{ it.hmsArtNr }.toSet()
            if (grunndataDeler.containsAll(manuelleDeler)) {
                println("$hmsnr har alle deler i grunndata som i manuell liste")
            } else {
                println("$hmsnr har deler i manuell liste: $manuelleDeler")
                println("$hmsnr har deler i grunndata: $grunndataDeler")
            }
        }
    }
}

