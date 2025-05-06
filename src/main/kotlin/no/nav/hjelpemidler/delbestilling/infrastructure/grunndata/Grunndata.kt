package no.nav.hjelpemidler.delbestilling.infrastructure.grunndata

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import no.nav.hjelpemidler.delbestilling.delbestilling.model.Hmsnr
import java.util.UUID

private val log = KotlinLogging.logger {}

class Grunndata(private val client: GrunndataClientInterface) {

    suspend fun hentProdukt(hmsnr: Hmsnr): Produkt? {
        log.info { "Henter produkt $hmsnr fra grunndata" }
        val produkt = client.hentProdukt(hmsnr).produkt
        log.info { "Produkt for $hmsnr: $produkt" }
        return produkt
    }

    suspend fun hentDeler(seriesId: UUID, produktId: UUID): List<Produkt> {
        log.info { "Henter deler for seriesId $seriesId og produktId $produktId fra grunndata" }
        return client.hentDeler(seriesId = seriesId, produktId = produktId).produkter
    }

    suspend fun hentAlleDelerSomKanBestilles(): List<Produkt> {
        log.info { "Henter alle deler som kan bestilles fra grunndata" }
        return client.hentAlleDelerSomKanBestilles().produkter
    }

    suspend fun hentAlleHjmMedIdEllerSeriesId(seriesIds: Set<UUID>, produktIds: Set<UUID>): List<Produkt> {
        log.info { "Henter alle hjm med gitte id eller seriesId fra grunndata" }
        return client.hentAlleHjmMedIdEllerSeriesId(seriesIds = seriesIds, produktIds = produktIds).produkter
    }
}