package no.nav.hjelpemidler.delbestilling.fakes

import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.GrunndataClientInterface
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.Hits
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.ProduktResponse
import no.nav.hjelpemidler.delbestilling.infrastructure.jsonMapper
import no.nav.hjelpemidler.delbestilling.testdata.Testdata
import java.util.UUID

object GrunndataTestHmsnr {
    const val UTEN_DELER_I_GRUNNDATA = "185144"
    const val KUN_GRUNNDATA_DELER = "167624"
    const val GRUNNDATA_OG_MANUELL = "316165"
    const val HAR_BATTERI = "250464"

    const val IKKE_I_GRUNNDATA = "097765"
}

class GrunndataClientFake() : GrunndataClientInterface {

    private fun loadResponse(fileName: String): ProduktResponse {
        val text = this::class.java.classLoader.getResource("testdata/grunndata/$fileName")?.readText()
            ?: return ProduktResponse(hits = Hits(emptyList()))
        return jsonMapper.readValue(text, ProduktResponse::class.java)
    }

    override suspend fun hentProdukt(hmsnr: String): ProduktResponse {
        return loadResponse("produkt_$hmsnr.json")
    }

    override suspend fun hentDeler(seriesId: UUID, produktId: UUID): ProduktResponse {
        return loadResponse("deler_${seriesId}_$produktId.json")
    }

    override suspend fun hentAlleDelerSomKanBestilles(): ProduktResponse {
        error("hentAlleDelerSomKanBestilles er ikke implementert i fake")
    }

    override suspend fun hentAlleHjmMedIdEllerSeriesId(seriesIds: Set<UUID>, produktIds: Set<UUID>): ProduktResponse {
        error("hentAlleHjmMedIdEllerSeriesId er ikke implementert i fake")
    }
}

