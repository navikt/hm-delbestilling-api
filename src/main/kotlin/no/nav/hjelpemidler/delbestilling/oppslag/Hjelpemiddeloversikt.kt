package no.nav.hjelpemidler.delbestilling.oppslag

import com.github.benmanes.caffeine.cache.AsyncLoadingCache
import com.github.benmanes.caffeine.cache.Caffeine
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import no.nav.hjelpemidler.cache.refreshAfterWrite
import no.nav.hjelpemidler.delbestilling.common.Hmsnr
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
    val finnDelerTilHjelpemiddel: FinnDelerTilHjelpemiddel,
    private val scope: CoroutineScope,
    private val cacheDuration: Duration = 2.hours
) {
    private val cacheKeyTilgjengeligeHjelpemidler = "tilgjengeligeHjelpemidler"

    private val cacheTilgjengeligeHjelpemidler: AsyncLoadingCache<String, Map<String, List<Hmsnr>>> = Caffeine.newBuilder()
        .refreshAfterWrite(cacheDuration)
        .maximumSize(1)
        .buildAsync { _, _ -> scope.future { hentTilgjengeligeHjelpemidler() } }

    suspend fun hentTilgjengeligeHjelpemidlerCached(): Map<String, List<Hmsnr>> {
        return cacheTilgjengeligeHjelpemidler.get(cacheKeyTilgjengeligeHjelpemidler).await()
    }

    suspend fun hentTilgjengeligeHjelpemidler(): Map<String, List<Hmsnr>> {
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

        // Lag en map over tittel og alle hmsnrs som har den tittelen i grunndata
        val grunndataHjelpemidler = alleHjelpemidlerSomHarDeler
            .groupBy { it.title.trim() }
            .mapValues { hm ->
                hm.value.map {
                    it.hmsArtNr
                }.distinct()
            }

        // Lag en map over tittel og alle hmsnrs som har den tittelen i manuell liste
        val manuelleHjelpemidler = hjelpemidlerFraManuellListe()

        // Kombiner dem samme
        val kombinert = (grunndataHjelpemidler.keys + manuelleHjelpemidler.keys)
            .associateWith { key ->
                val fraGrunndata = grunndataHjelpemidler[key].orEmpty()
                val fraManuelle = manuelleHjelpemidler[key].orEmpty()
                (fraGrunndata + fraManuelle).distinct()
            }

        val sortert = kombinert.toSortedMap().mapValues { (_, value) -> value.sorted() }

        return sortert
    }

    suspend fun hentDelerTilHmsnrs (hmsnrs: List<String>): List<String> = coroutineScope {
        val delerNavn = hmsnrs.map { hmsnr ->
            async {
                val hm = finnDelerTilHjelpemiddel(hmsnr)
                hm.deler.map {del -> del.navn }
            }
        }.awaitAll().flatten().distinct().sorted()

        delerNavn
    }

    fun startBakgrunnsjobb() {
        log.info { "Starter bakgrunnsjobber for Hjelpemiddeloversikt" }
        scope.launch {
            log.info { "Prepopulerer hjelpemidler cache" }
            measureTimeMillis {
                cacheTilgjengeligeHjelpemidler.get(cacheKeyTilgjengeligeHjelpemidler).await()
            }.also { log.info { "Prepopulering tok $it ms." } }
        }

        scope.launch {
            while (isActive) {
                delay(cacheDuration.plus(1.seconds)) // Legg på 1 sek for å sikre at cachen er utdatert før vi refresher
                log.info { "Refresher hjelpemiddel cache" }
                cacheTilgjengeligeHjelpemidler.synchronous().refresh(cacheKeyTilgjengeligeHjelpemidler)
            }
        }
        log.info { "Bakgrunnsjobber for Hjelpemiddeloversikt startet." }
    }
}

private fun hjelpemidlerFraManuellListe(): Map<String, List<String>> {
    val hjmNavnRegex = Regex("^(.*?)\\s(sb\\d+|K|L|Led|HD|sd\\d+|voksen).*$")

    val hjelpemidler = hmsnrTilHjelpemiddelnavn.values
        .groupBy { produkt ->
            val match = hjmNavnRegex.find(produkt.navn)
            match?.groups?.get(1)?.value?.trim() ?: produkt.navn
        }
        .mapValues { entry ->
            entry.value.map { it.hmsnr }.distinct()
        }

    return hjelpemidler
}
