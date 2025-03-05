package no.nav.hjelpemidler.delbestilling.grunndata

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hmsnrHjmTilHmsnrDeler
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hmsnrTilHjelpemiddel
import org.junit.Ignore
import org.junit.jupiter.api.Test

// Telling på antall deler fra grunndata vs delbestilling
fun main() = runBlocking {
    val grunndataClient = GrunndataClient()

    val alleHmsnr = hmsnrTilHjelpemiddel.keys.toList()
    var countDelerDelbestilling = 0
    var countDelerGrunndata = 0
    var countHjelpemiddelFinnesIkkeIGrunndata = 0
    var countHjelpemiddelMedFærreDelerIGrunndata = 0

    alleHmsnr.forEach { hmsnr ->
        val hjelpemiddel = grunndataClient.hentHjelpemiddel(hmsnr).produkt
        val delerDelbestilling = hmsnrHjmTilHmsnrDeler[hmsnr]!!
        countDelerDelbestilling += delerDelbestilling.size

        if (hjelpemiddel == null) {
            countHjelpemiddelFinnesIkkeIGrunndata++
            println("$hmsnr finnes ikke i grunndata")
            countHjelpemiddelMedFærreDelerIGrunndata++
        } else {
            val seriesId = hjelpemiddel.seriesId
            val produktId = hjelpemiddel.id
            val delerGrunndata = grunndataClient.hentDeler(seriesId, produktId).produkter

            countDelerGrunndata += delerGrunndata.size

            if (delerGrunndata.size < delerDelbestilling.size) {
                countHjelpemiddelMedFærreDelerIGrunndata++
            }

            println("$hmsnr grunndata: ${delerGrunndata.size}, delbestilling: ${delerDelbestilling.size}")
        }
    }

    println("Totalt antall deler => grunndata:$countDelerGrunndata, delbestilling: $countDelerDelbestilling")
    println("Antall hjelpemiddel som ikke finnes i grunndata: $countHjelpemiddelFinnesIkkeIGrunndata")
}

internal class GrunndataClientTest {
    @Test
    @Ignore
    fun `tell deler for hjelpemiddel`() = runTest {
        val hmsnr = "301998"
        val grunndataClient = GrunndataClient()
        val result = grunndataClient.hentHjelpemiddel(hmsnr)
        val hjelpemiddel = result.produkt
        val seriesId = hjelpemiddel!!.seriesId
        val produktId = hjelpemiddel!!.id
        val delerGrunndata = grunndataClient.hentDeler(seriesId, produktId).produkter

        val delerDelbestilling = hmsnrHjmTilHmsnrDeler[hmsnr]!!

        println("$hmsnr grunndata: ${delerGrunndata.size}, delbestilling: ${delerDelbestilling.size}")
        delerGrunndata.forEach {
            println(it.articleName)
        }
    }
}
