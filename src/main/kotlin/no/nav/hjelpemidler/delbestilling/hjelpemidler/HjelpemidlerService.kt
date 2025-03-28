package no.nav.hjelpemidler.delbestilling.hjelpemidler

import com.github.benmanes.caffeine.cache.Caffeine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hmsnrTilHjelpemiddel
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.Grunndata
import java.util.concurrent.TimeUnit


class HjelpemidlerService(
    val grunndata: Grunndata,
) {

    private val cache = Caffeine.newBuilder()
        .expireAfterWrite(2, TimeUnit.HOURS)
        .maximumSize(1)
        .build<String, Deferred<Set<String>>>()

    suspend fun hentAlleHjelpemiddelTitler(): Set<String> = cache.get("hjelpemiddeltittler") {
        CoroutineScope(Dispatchers.IO).async {
            val alleDelerSomKanBestilles = grunndata.hentAlleDelerSomKanBestilles()
            val produktIDs = alleDelerSomKanBestilles.map {
                it.attributes.compatibleWith?.productIds ?: emptyList()
            }.flatten().toSet()
            val serieIDs = alleDelerSomKanBestilles.map {
                it.attributes.compatibleWith?.seriesIds ?: emptyList()
            }.flatten().toSet()
            val hjelpemidler = grunndata.hentAlleHjmMedIdEllerSeriesId(seriesIds = serieIDs, produktIds = produktIDs)

            val hjelpemiddelNavnFraGrunndata = hjelpemidler.map { it.title.trim() }.toSet()

            (hjelpemiddelNavnFraGrunndata + hjelpemiddelNavnFraManuellListe()).toSortedSet()
        }
    }.await()

}

private fun hjelpemiddelNavnFraManuellListe(): Set<String> {
    val hjmNavnRegex = Regex("^(.*?)\\s(sb\\d+|K|L|Led|HD|sd\\d+|voksen).*$")

    val hjelpemiddelNavn = hmsnrTilHjelpemiddel.values.map {
        val match = hjmNavnRegex.find(it.navn)
        match?.groups?.get(1)?.value ?: it.navn
    }.toSet()

    return hjelpemiddelNavn
}
