package no.nav.hjelpemidler.delbestilling.testdata

import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.hjelpemidler.delbestilling.delbestilling.model.Kilde
import no.nav.hjelpemidler.delbestilling.fakes.GrunndataTestHmsnr
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.Grunndata
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.GrunndataClient
import no.nav.hjelpemidler.delbestilling.infrastructure.jsonMapper
import no.nav.hjelpemidler.delbestilling.oppslag.FinnDelerTilHjelpemiddel
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.data.hmsnr2Hjm
import java.io.File

fun main() {
    runBlocking {
        oppdaterTestdata()
        //finnHjelpemiddelIGrunndataMenMedKunManuelleDeler()
    }
}

private suspend fun oppdaterTestdata() {
    listOf("185144").forEach { lagreProduktOgDeler(it) }
}

private suspend fun lagreProduktOgDeler(hmsnr: String) {
    val client = client()
    val path = "src/test/resources/testdata/grunndata"

    val produktResponse = client.hentProdukt(hmsnr)
    val produktJson = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(produktResponse)
    File("$path/produkt_$hmsnr.json").writeText(produktJson)

    val produkt = produktResponse.produkt
    if (produkt != null) {
        val delerResponse = client.hentDeler(seriesId = produkt.seriesId, produktId = produkt.id)
        val delerJson = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(delerResponse)
        File("$path/deler_${produkt.seriesId}_${produkt.id}.json")
            .writeText(delerJson)
    }
}

private suspend fun finnHjelpemiddelIGrunndataMenMedKunManuelleDeler() {
    val grunndata = Grunndata(client())
    val finnDelerTilHjelpemiddel = FinnDelerTilHjelpemiddel(grunndata, mockk(relaxed = true), mockk(relaxed = true))
    hmsnr2Hjm.keys.forEach {
        if (grunndata.hentProdukt(it) == null) {
            return@forEach
        }

        val foo = finnDelerTilHjelpemiddel.execute(it)

        if (foo.deler.all { it.kilde == Kilde.MANUELL_LISTE }) {
            println("$foo har deler bare i manuell liste")
            return
        }
    }
}

private fun client() = GrunndataClient(baseUrl = "https://finnhjelpemiddel.nav.no")
