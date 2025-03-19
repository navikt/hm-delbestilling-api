package no.nav.hjelpemidler.delbestilling.hjelpemidler

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import no.nav.hjelpemidler.delbestilling.delbestilling.AlleHjelpemidlerMedDelerResultat
import no.nav.hjelpemidler.delbestilling.grunndata.GrunndataClient
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hmsnr2Hjm
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hmsnrTilHjelpemiddel

private val logger = KotlinLogging.logger { }

class HjelpemidlerService(
    val grunndataClient: GrunndataClient,
) {

    fun hentAlleHjelpemidlerMedDeler(): AlleHjelpemidlerMedDelerResultat {
        val alleHjelpemidlerMedDeler = hmsnr2Hjm.values.toList()
        return AlleHjelpemidlerMedDelerResultat(alleHjelpemidlerMedDeler, HttpStatusCode.OK)
    }

    suspend fun hentAlleHjelpemiddelTitler(): Set<String> {
        val alleDelerSomKanBestilles = grunndataClient.hentAlleDelerSomKanBestilles()
        val produktIDs = alleDelerSomKanBestilles.produkter.map {
            it.attributes.compatibleWith?.productIds ?: emptyList()
        }.flatten().toSet()
        val serieIDs = alleDelerSomKanBestilles.produkter.map {
            it.attributes.compatibleWith?.seriesIds ?: emptyList()
        }.flatten().toSet()
        val hjelpemidler = grunndataClient.hentAlleHjmMedIdEllerSeriesId(seriesIds = serieIDs, produktIds = produktIDs)

        val hjelpemiddelNavnFraGrunndata = hjelpemidler.produkter.map { it.title.trim() }.toSet()

        return (hjelpemiddelNavnFraGrunndata + hjelpemiddelNavnFraManuellListe()).toSortedSet()
    }

}

private fun hjelpemiddelNavnFraManuellListe(): Set<String> {
    val hjmNavnRegex = Regex("^(.*?)\\s(sb\\d+|K|L|Led|HD|sd\\d+|voksen).*$")

    val hjelpemiddelNavn = hmsnrTilHjelpemiddel.values.map {
        val match = hjmNavnRegex.find(it.navn)
        match?.groups?.get(1)?.value ?: it.navn
    }.toSet()

    return hjelpemiddelNavn
}
