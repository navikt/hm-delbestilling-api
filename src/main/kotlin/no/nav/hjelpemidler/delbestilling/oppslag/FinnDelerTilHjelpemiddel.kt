package no.nav.hjelpemidler.delbestilling.oppslag

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.delbestilling.model.Hmsnr
import no.nav.hjelpemidler.delbestilling.delbestilling.model.Kilde
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.Grunndata
import no.nav.hjelpemidler.delbestilling.infrastructure.metrics.Metrics
import no.nav.hjelpemidler.delbestilling.infrastructure.slack.Slack
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.data.hmsnr2Hjm
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.defaultAntall
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.maksAntall

private val log = KotlinLogging.logger {}

class FinnDelerTilHjelpemiddel(
    private val grunndata: Grunndata,
    private val slack: Slack,
    private val metrics: Metrics,
) {

    suspend fun execute(hmsnr: Hmsnr): Hjelpemiddel {
        log.info { "Henter deler for hjelpemiddel $hmsnr" }

        val hjmManuellListe = hmsnr2Hjm[hmsnr]
        val hjmGrunndata = hentHjelpemiddelFraGrunndata(hmsnr)

        sendStatistikkOgVarsling(hjmGrunndata = hjmGrunndata, hjmManuellListe = hjmManuellListe)

        val hjelpemiddel = slåSammen(hjmGrunndata, hjmManuellListe)

        if (hjelpemiddel == null) {
            slack.varsleOmManglendeHmsnr(hmsnr)
            throw TilbyrIkkeHjelpemiddelException("Fant ikke $hmsnr verken i grunndata eller manuell liste")
        }

        if (hjelpemiddel.deler.isEmpty()) {
            throw TilbyrIkkeHjelpemiddelException("Fant ingen deler i verken grunndata eller manuell liste for $hmsnr")
        }

        sjekkBatteri(hjelpemiddel.deler)

        log.info { "hjelpemiddel for $hmsnr: $hjelpemiddel" }
        return hjelpemiddel
    }

    private suspend fun hentHjelpemiddelFraGrunndata(hmsnr: Hmsnr): Hjelpemiddel? {
        try {
            val produkt = grunndata.hentProdukt(hmsnr)

            if (produkt == null) {
                log.info { "Fant ikke ${hmsnr} i grunndata" }
                return null
            }

            if (!produkt.main) {
                throw IkkeHjelpemiddelException("Hmsnr $hmsnr er ikke hjelpemiddel.")
            }

            val deler = grunndata.hentDeler(produkt.seriesId, produkt.id)
            log.info { "Fant hmsnr ${produkt.hmsArtNr} ${produkt.articleName} i grunndata. Denne har ${deler.size} egnede deler fra grunndata knyttet til seg" }

            return Hjelpemiddel(
                navn = produkt.articleName,
                hmsnr = produkt.hmsArtNr,
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
                        imgs = bilder,
                    )
                })
        } catch (e: Exception) {
            log.info(e) { "Klarte ikke å sjekke $hmsnr i grunndata" }
            return null
        }
    }

    private fun sendStatistikkOgVarsling(hjmGrunndata: Hjelpemiddel?, hjmManuellListe: Hjelpemiddel?) {
        log.info { "Treff i manuell liste: $hjmManuellListe" }
        log.info { "Treff i grunndata: $hjmGrunndata" }

        if (hjmGrunndata?.deler?.isEmpty() == true) {
            slack.varsleOmIngenDelerTilGrunndataHjelpemiddel(
                hmsnr = hjmGrunndata.hmsnr,
                navn = hjmGrunndata.navn,
                delerIManuellListe = hjmManuellListe?.deler ?: emptyList()
            )
            metrics.grunndataHjelpemiddelManglerDeler(
                hjmGrunndata.hmsnr,
                hjmGrunndata.navn
            )
        }

        sjekkOmGrunndataDekkerManuellListeForHjm(hjmManuellListe, hjmGrunndata)
    }

    private fun sjekkOmGrunndataDekkerManuellListeForHjm(
        manuell: Hjelpemiddel?,
        grunndata: Hjelpemiddel?
    ) {
        if (manuell == null || grunndata == null) {
            return
        }
        val hmsnrManuell = manuell.deler.map { it.hmsnr }.toSet()
        val hmsnrGrunndata = grunndata.deler.map { it.hmsnr }.toSet()

        if (hmsnrGrunndata.containsAll(hmsnrManuell)) {
            slack.varsleGrunndataDekkerManuellListeForHjelpemiddel(manuell.hmsnr, manuell.navn)
        }
    }

    private fun sjekkBatteri(deler: List<Del>) {
        // For sjekk av hvilke deler som inneholder "batteri" i navnet, for å se om vi må utvide batteri-sjekk
        val håndterteBatterikategorier = setOf("batteri", "batterikabel", "batterilader", "batterideksel", "batteripakke", "batteriboks")

        val delerMedBatteriIKategori = deler
            .filter { del ->
                val kategori = del.kategori.lowercase()
                kategori.contains("batteri") && kategori !in håndterteBatterikategorier
            }

        if (delerMedBatteriIKategori.isNotEmpty()) {
            log.info { "Deler med uverifisert batterikategori: $delerMedBatteriIKategori" }
            slack.varsleOmPotensiellBatteriKategorier(delerMedBatteriIKategori)
        }
    }
}

private fun slåSammen(hjmGrunndata: Hjelpemiddel?, hjmManuellListe: Hjelpemiddel?): Hjelpemiddel? {
    return if (hjmGrunndata != null) {
        Hjelpemiddel(
            navn = hjmGrunndata.navn,
            hmsnr = hjmGrunndata.hmsnr,
            deler = hjmGrunndata.deler.berikMedUnikeDeler(hjmManuellListe?.deler)
        )
    } else hjmManuellListe
}

private fun List<Del>.berikMedUnikeDeler(other: List<Del>?): List<Del> {
    val eksisterendeHmsnr = this.map { it.hmsnr }
    val nyeDeler = other?.filterNot {
        it.hmsnr in eksisterendeHmsnr
    } ?: emptyList()
    return this + nyeDeler
}