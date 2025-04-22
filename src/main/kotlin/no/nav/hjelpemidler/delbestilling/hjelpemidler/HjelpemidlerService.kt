package no.nav.hjelpemidler.delbestilling.hjelpemidler

import com.github.benmanes.caffeine.cache.AsyncLoadingCache
import com.github.benmanes.caffeine.cache.Caffeine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import no.nav.hjelpemidler.cache.refreshAfterWrite
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hmsnrTilHjelpemiddel
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.Grunndata
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds


class HjelpemidlerService(
    val grunndata: Grunndata,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    cacheDuration: Duration = 2.hours
) {

    private val cacheKey = "hjelpemidler"

    private val cache: AsyncLoadingCache<String, Set<String>> = Caffeine.newBuilder()
        .refreshAfterWrite(cacheDuration)
        .maximumSize(1)
        .buildAsync { _, _ -> scope.future { hentAlleHjelpemiddelTitler() } }

    init {
        // Prepopuler cachen og refresh den jevnlig
        scope.launch {
            while (isActive) {
                cache.synchronous().refresh(cacheKey)
                delay(cacheDuration.plus(1.seconds)) // Legg på 1 sek for å sikre at cachen er utdatert før vi refresher
            }
        }
    }

    suspend fun hentAlleHjelpemiddelTitlerCached(): Set<String> {
        return cache.get(cacheKey).await()
    }

    private suspend fun hentAlleHjelpemiddelTitler(): Set<String> {
        val alleDelerSomKanBestilles = grunndata.hentAlleDelerSomKanBestilles()
        val produktIDs = alleDelerSomKanBestilles.map {
            it.attributes.compatibleWith?.productIds ?: emptyList()
        }.flatten().toSet()
        val serieIDs = alleDelerSomKanBestilles.map {
            it.attributes.compatibleWith?.seriesIds ?: emptyList()
        }.flatten().toSet()
        val hjelpemidler = grunndata.hentAlleHjmMedIdEllerSeriesId(seriesIds = serieIDs, produktIds = produktIDs)

        val hjelpemiddelNavnFraGrunndata = hjelpemidler.map { it.title.trim() }.toSet()

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