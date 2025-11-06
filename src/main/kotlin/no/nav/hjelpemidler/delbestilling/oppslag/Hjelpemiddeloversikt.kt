package no.nav.hjelpemidler.delbestilling.oppslag

import com.github.benmanes.caffeine.cache.AsyncLoadingCache
import com.github.benmanes.caffeine.cache.Caffeine
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import no.nav.hjelpemidler.cache.refreshAfterWrite
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.data.hmsnrTilHjelpemiddelnavn
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.Grunndata
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.Produkt
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.data.hmsnr2Hjm
import java.util.UUID
import kotlin.system.measureTimeMillis
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

private val log = KotlinLogging.logger {}

class Hjelpemiddeloversikt(
    val grunndata: Grunndata,
    private val scope: CoroutineScope,
    private val cacheDuration: Duration = 2.hours
) {

    private val cacheKey = "hjelpemidler"

    private val cache: AsyncLoadingCache<String, HjelpemiddeloversiktResponse> = Caffeine.newBuilder()
        .refreshAfterWrite(cacheDuration)
        .maximumSize(1)
        .buildAsync { _, _ -> scope.future { hentAlleHjelpemiddelTitler() } }

    suspend fun hentAlleHjelpemiddelTitlerCached(): HjelpemiddeloversiktResponse {
        return cache.get(cacheKey).await()
    }

    // Deprecated. Skal erstattes av hentTilgjengeligeHjelpemidler()
    private suspend fun hentAlleHjelpemiddelTitler(): HjelpemiddeloversiktResponse {
        val alleDelerSomKanBestilles = grunndata.hentAlleDelerSomKanBestilles()
        val produktIDs = alleDelerSomKanBestilles.map {
            it.attributes.compatibleWith?.productIds ?: emptyList()
        }.flatten().toSet()
        val serieIDs = alleDelerSomKanBestilles.map {
            it.attributes.compatibleWith?.seriesIds ?: emptyList()
        }.flatten().toSet()
        val hjelpemidler = grunndata.hentAlleHjmMedIdEllerSeriesId(seriesIds = serieIDs, produktIds = produktIDs)

        val hjelpemiddelNavnFraGrunndata = hjelpemidler.map { it.title.trim() }.toSet()

        return HjelpemiddeloversiktResponse((hjelpemiddelNavnFraGrunndata + hjelpemiddelNavnFraManuellListe()).toSortedSet())
    }

    // TODO: caching
    suspend fun hentTilgjengeligeHjelpemidler(): TilgjengeligeHjelpemidlerResponse {
        val alleDelerSomKanBestilles = grunndata.hentAlleDelerSomKanBestilles()
        val produktIDs = alleDelerSomKanBestilles.map {
            it.attributes.compatibleWith?.productIds ?: emptyList()
        }.flatten().toSet()
        val serieIDs = alleDelerSomKanBestilles.map {
            it.attributes.compatibleWith?.seriesIds ?: emptyList()
        }.flatten().toSet()

        // Her er ALLE hjelpemidler, uavhengig av title. Dvs, det kan være f.els 3 stk med title="Cross", med 3 ulike hmsnrs som igjen har ulike deler
        val alleHjelpemidlerSomHarDeler =
            grunndata.hentAlleHjmMedIdEllerSeriesId(seriesIds = serieIDs, produktIds = produktIDs)

        val tilgjengeligeHjelpemidlerMedDeler = alleHjelpemidlerSomHarDeler
            .map { hm ->
                TilgjengeligHjelpemiddel(
                    navn = hm.title.trim(), // flere hjelpemidler deler title
                    delerNavn = alleDelerSomKanBestilles.filter { del ->
                        del.attributes.egnetForKommunalTekniker == true &&
                                del.attributes.compatibleWith?.productIds?.contains(hm.id) == true || del.attributes.compatibleWith?.seriesIds?.contains(
                            hm.seriesId
                        ) == true
                    }
                        .map { it.title.trim() }
                        .plus(hmsnr2Hjm[hm.hmsArtNr]?.deler?.map { it.navn }
                            ?: emptyList()) // legg til deler fra manuell liste
                )
            }
            .sortedBy { it.navn }
            .groupBy { it.navn }
            .map { (navn, group) -> // slå sammen alle hjelpemidler som har samme navn, og deres unike deler
                TilgjengeligHjelpemiddel(
                    navn = navn,
                    delerNavn = group.flatMap { it.delerNavn }.distinct()
                )
            }

        return TilgjengeligeHjelpemidlerResponse(tilgjengeligeHjelpemidlerMedDeler)
    }

    fun startBakgrunnsjobb() {
        log.info { "Starter bakgrunnsjobber for Hjelpemiddeloversikt" }
        scope.launch {
            log.info { "Prepopulerer hjelpemidler cache" }
            measureTimeMillis {
                cache.get(cacheKey).await()
            }.also { log.info { "Prepopulering tok $it ms." } }
        }

        scope.launch {
            while (isActive) {
                delay(cacheDuration.plus(1.seconds)) // Legg på 1 sek for å sikre at cachen er utdatert før vi refresher
                log.info { "Refresher hjelpemiddel cache" }
                cache.synchronous().refresh(cacheKey)
            }
        }
        log.info { "Bakgrunnsjobber for Hjelpemiddeloversikt startet." }
    }
}

private fun hjelpemiddelNavnFraManuellListe(): Set<String> {
    val hjmNavnRegex = Regex("^(.*?)\\s(sb\\d+|K|L|Led|HD|sd\\d+|voksen).*$")

    val hjelpemiddelNavn = hmsnrTilHjelpemiddelnavn.values.map {
        val match = hjmNavnRegex.find(it.navn)
        match?.groups?.get(1)?.value ?: it.navn
    }.toSet()

    return hjelpemiddelNavn
}