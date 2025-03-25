package no.nav.hjelpemidler.delbestilling.infrastructure.grunndata

import no.nav.hjelpemidler.delbestilling.delbestilling.Hmsnr
import no.nav.hjelpemidler.delbestilling.infrastructure.monitoring.Logg
import java.util.UUID

class Grunndata(private val client: GrunndataClient) {

    suspend fun hentHjelpemiddel(hmsnr: Hmsnr): Produkt? {
        Logg.info { "Henter hjelpemiddel $hmsnr fra grunndata" }
        return client.hentHjelpemiddel(hmsnr).produkt
    }

    suspend fun hentDeler(seriesId: UUID, produktId: UUID): List<Produkt> {
        Logg.info { "Henter deler for seriesId $seriesId og produktId $produktId fra grunndata" }
        return client.hentDeler(seriesId = seriesId, produktId = produktId).produkter
    }

    suspend fun hentAlleDelerSomKanBestilles(): List<Produkt> {
        Logg.info { "Henter alle deler som kan bestilles fra grunndata" }
        return client.hentAlleDelerSomKanBestilles().produkter
    }

    suspend fun hentAlleHjmMedIdEllerSeriesId(seriesIds: Set<UUID>, produktIds: Set<UUID>): List<Produkt> {
        Logg.info { "Henter alle hjm med gitte id eller seriesId fra grunndata" }
        return client.hentAlleHjmMedIdEllerSeriesId(seriesIds = seriesIds, produktIds = produktIds).produkter
    }
}