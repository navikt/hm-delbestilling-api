package no.nav.hjelpemidler.delbestilling.delbestilling

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import no.nav.hjelpemidler.delbestilling.delbestilling.model.Del
import no.nav.hjelpemidler.delbestilling.delbestilling.model.HjelpemiddelMedDeler
import no.nav.hjelpemidler.delbestilling.delbestilling.model.Kilde
import no.nav.hjelpemidler.delbestilling.delbestilling.model.Lagerstatus
import no.nav.hjelpemidler.delbestilling.delbestilling.model.OppslagFeil
import no.nav.hjelpemidler.delbestilling.delbestilling.model.OppslagResultat
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hmsnr2Hjm
import no.nav.hjelpemidler.delbestilling.hjelpemidler.defaultAntall
import no.nav.hjelpemidler.delbestilling.hjelpemidler.maksAntall
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.Grunndata

private val log = KotlinLogging.logger {}

class Hjelpemiddeldeler(
    private val grunndata: Grunndata,
) {
    suspend fun finnTilgjengeligeDeler(hmsnr: String): OppslagResultat {
        val hjelpemiddelMedDelerManuell = hmsnr2Hjm[hmsnr].also {
            if (it == null) {
                log.info { "Fant ikke ${hmsnr} i manuell liste" }
            }
        }

        val hjelpemiddelMedDelerGrunndata = try {
            val grunndataHjelpemiddel = grunndata.hentProdukt(hmsnr)

            if (grunndataHjelpemiddel != null) {
                if (!grunndataHjelpemiddel.main) {
                    return OppslagResultat(null, OppslagFeil.IKKE_HOVEDHJELPEMIDDEL, HttpStatusCode.NotFound)
                }
                val deler = grunndata.hentDeler(grunndataHjelpemiddel.seriesId, grunndataHjelpemiddel.id)
                if (deler.isEmpty()) {
                    log.info { "Fant hmsnr $hmsnr i grunndata, men den har ingen egnede deler knyttet til seg" }
                }

                val hjelpemiddelMedDeler =
                    HjelpemiddelMedDeler(
                        navn = grunndataHjelpemiddel.articleName,
                        hmsnr = grunndataHjelpemiddel.hmsArtNr,
                        deler = deler.map {
                            val kategori = it.articleName.split(" ").first()
                            val bilder = it.bildeUrls(it.hmsArtNr)
                            Del(
                                hmsnr = it.hmsArtNr,
                                navn = it.articleName,
                                levArtNr = it.supplierRef,
                                kategori = kategori,
                                maksAntall = maksAntall(kategori, it.isoCategory),
                                kilde = Kilde.GRUNNDATA,
                                defaultAntall = defaultAntall(kategori),
                                img = bilder.firstOrNull(),
                                imgs = bilder,
                            )
                        })

                log.info { "Fant hmsnr ${hjelpemiddelMedDeler.hmsnr} ${hjelpemiddelMedDeler.navn} i grunndata. Denne har ${hjelpemiddelMedDeler.deler.size} egnede deler fra grunndata knyttet til seg" }
                hjelpemiddelMedDeler
            } else {
                log.info { "Fant ikke ${hmsnr} i grunndata" }
                null
            }
        } catch (e: Exception) {
            log.info(e) { "Klarte ikke å sjekke $hmsnr i grunndata" }
            null
        }

        val deler = mutableListOf<Del>()

        if (hjelpemiddelMedDelerGrunndata != null) {
            deler.addAll(hjelpemiddelMedDelerGrunndata.deler)
        }

        if (hjelpemiddelMedDelerManuell != null) {
            hjelpemiddelMedDelerManuell.deler.forEach { del ->
                if (!deler.any { it.hmsnr == del.hmsnr }) {
                    deler.add(del)
                }
            }
        }

        val hjelpemiddelMedDeler = if (hjelpemiddelMedDelerGrunndata != null) {
            HjelpemiddelMedDeler(
                navn = hjelpemiddelMedDelerGrunndata.navn,
                hmsnr = hjelpemiddelMedDelerGrunndata.hmsnr,
                deler = deler
            )
        } else if (hjelpemiddelMedDelerManuell != null) {
            HjelpemiddelMedDeler(
                navn = hjelpemiddelMedDelerManuell.navn,
                hmsnr = hjelpemiddelMedDelerManuell.hmsnr,
                deler = deler
            )
        } else {
            null
        }

        log.info { "hjelpemiddelMedDeler: $hjelpemiddelMedDeler" }

        if (hjelpemiddelMedDeler == null) {
            log.info { "Fant $hmsnr verken i grunndata eller manuell liste, returnerer TILBYR_IKKE_HJELPEMIDDEL" }
            return OppslagResultat(null, OppslagFeil.TILBYR_IKKE_HJELPEMIDDEL, HttpStatusCode.NotFound)
        }

        if (hjelpemiddelMedDeler.deler.isEmpty()) {
            log.info { "Fant ingen deler i verken grunndata eller manuell liste for $hmsnr, returnerer TILBYR_IKKE_HJELPEMIDDEL" }
            return OppslagResultat(null, OppslagFeil.TILBYR_IKKE_HJELPEMIDDEL, HttpStatusCode.NotFound)
        }

        // For sjekk av hvilke deler som inneholder "batteri" i navnet, for å se om vi må utvide batteri-sjekk
        val delerMedBatteriINavn =
            hjelpemiddelMedDeler.deler.filter { it.navn.lowercase().contains("batteri") }.map { it.navn }.toSet()
        if (delerMedBatteriINavn.isNotEmpty()) {
            log.info { "Deler med 'batteri' i navnet på oppslag: $delerMedBatteriINavn" }
        }

        // Sorter på navn
        hjelpemiddelMedDeler.deler = hjelpemiddelMedDeler.deler.sortedBy { it.navn }

        // legg på pseudo-random lagerstatus
        hjelpemiddelMedDeler.deler.forEach {
            val erMinmax = it.hmsnr.toInt() % 3 != 0                // Gjør ca 66% tilgjengelig
            val antallPåLager = it.hmsnr.takeLast(1).toInt()    // Antall tilgjengelig = siste siffer i hmsnr
            it.lagerstatus = Lagerstatus(
                organisasjons_id = 292,
                organisasjons_navn = "*19 Troms",
                artikkelnummer = it.hmsnr,
                minmax = erMinmax,
                tilgjengelig = antallPåLager,
                antallDelerPåLager = antallPåLager
            )
        }

        return OppslagResultat(hjelpemiddelMedDeler, null, HttpStatusCode.OK)
    }
}