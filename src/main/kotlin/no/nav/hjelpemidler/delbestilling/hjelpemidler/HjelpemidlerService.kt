package no.nav.hjelpemidler.delbestilling.hjelpemidler

import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hmsnrTilHjelpemiddel
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.Grunndata


class HjelpemidlerService(
    val grunndata: Grunndata,
) {
    suspend fun hentAlleHjelpemiddelTitler(): Set<String> {
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
