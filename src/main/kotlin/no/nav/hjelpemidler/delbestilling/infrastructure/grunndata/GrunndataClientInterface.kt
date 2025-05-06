package no.nav.hjelpemidler.delbestilling.infrastructure.grunndata

import java.util.UUID

interface GrunndataClientInterface {
    suspend fun hentProdukt(hmsnr: String): ProduktResponse
    suspend fun hentDeler(seriesId: UUID, produktId: UUID): ProduktResponse
    suspend fun hentAlleDelerSomKanBestilles(): ProduktResponse
    suspend fun hentAlleHjmMedIdEllerSeriesId(seriesIds: Set<UUID>, produktIds: Set<UUID>): ProduktResponse
}