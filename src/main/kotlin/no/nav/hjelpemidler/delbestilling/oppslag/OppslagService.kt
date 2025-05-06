package no.nav.hjelpemidler.delbestilling.oppslag

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import no.nav.hjelpemidler.delbestilling.config.isDev
import no.nav.hjelpemidler.delbestilling.delbestilling.PiloterService
import no.nav.hjelpemidler.delbestilling.delbestilling.model.Del
import no.nav.hjelpemidler.delbestilling.delbestilling.model.HjelpemiddelMedDeler
import no.nav.hjelpemidler.delbestilling.delbestilling.model.Kilde
import no.nav.hjelpemidler.delbestilling.delbestilling.model.OppslagFeil
import no.nav.hjelpemidler.delbestilling.delbestilling.model.OppslagResultat
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.Grunndata
import no.nav.hjelpemidler.delbestilling.infrastructure.metrics.Metrics
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Oebs
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.Pdl
import no.nav.hjelpemidler.delbestilling.infrastructure.slack.Slack
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.data.hmsnr2Hjm
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.defaultAntall
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.maksAntall


private val log = KotlinLogging.logger {}

class OppslagService(
    private val pdl: Pdl,
    private val oebs: Oebs,
    private val metrics: Metrics,
    private val slack: Slack,
    private val grunndata: Grunndata,
    private val piloterService: PiloterService,
    private val hjelpemiddeldelerDev: HjelpemiddeldelerDev,
) {

    suspend fun slåOppHjelpemiddel(hmsnr: String): OppslagResultat {
        log.info { "Slår opp deler til hjelpemiddel $hmsnr" }
        return hjelpemiddeldelerDev.finnTilgjengeligeDeler(hmsnr)
    }

    suspend fun slåOppHjelpemiddel(hmsnr: String, serienr: String): OppslagResultat = coroutineScope {
        val brukersKommunenummerResult = async {
            val brukersFnr = oebs.hentFnrLeietaker(hmsnr, serienr)
                ?: return@async null
            pdl.hentKommunenummer(brukersFnr)
        }

        val hjelpemiddelMedDelerManuell = hmsnr2Hjm[hmsnr].also {
            if (it == null) {
                log.info { "Fant ikke ${hmsnr} i manuell liste" }
            }
        }

        val hjelpemiddelMedDelerGrunndata = try {
            val grunndataHjelpemiddel = grunndata.hentProdukt(hmsnr)

            if (grunndataHjelpemiddel != null) {
                if (!grunndataHjelpemiddel.main) {
                    return@coroutineScope OppslagResultat(null, OppslagFeil.IKKE_HOVEDHJELPEMIDDEL, HttpStatusCode.NotFound)
                }

                val deler = grunndata.hentDeler(grunndataHjelpemiddel.seriesId, grunndataHjelpemiddel.id)
                if (deler.isEmpty()) {
                    log.info { "Fant hmsnr $hmsnr i grunndata, men den har ingen egnede deler knyttet til seg" }
                    slack.varsleOmIngenDelerTilGrunndataHjelpemiddel(
                        produkt = grunndataHjelpemiddel,
                        delerIManuellListe = hjelpemiddelMedDelerManuell?.deler ?: emptyList()
                    )
                    metrics.grunndataHjelpemiddelManglerDeler(
                        grunndataHjelpemiddel.hmsArtNr,
                        grunndataHjelpemiddel.articleName
                    )
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

        sjekkOmGrunndataDekkerManuellListeForHjm(hjelpemiddelMedDelerManuell, hjelpemiddelMedDelerGrunndata)

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
            slack.varsleOmManglendeHmsnr(hmsnr)
            return@coroutineScope OppslagResultat(null, OppslagFeil.TILBYR_IKKE_HJELPEMIDDEL, HttpStatusCode.NotFound)
        }

        if (hjelpemiddelMedDeler.deler.isEmpty()) {
            log.info { "Fant ingen deler i verken grunndata eller manuell liste for $hmsnr, returnerer TILBYR_IKKE_HJELPEMIDDEL" }
            return@coroutineScope OppslagResultat(null, OppslagFeil.TILBYR_IKKE_HJELPEMIDDEL, HttpStatusCode.NotFound)
        }

        // For sjekk av hvilke deler som inneholder "batteri" i navnet, for å se om vi må utvide batteri-sjekk
        val delerMedBatteriINavn =
            hjelpemiddelMedDeler.deler.filter { it.navn.lowercase().contains("batteri") }.map { it.navn }.toSet()
        if (delerMedBatteriINavn.isNotEmpty()) {
            log.info { "Deler med 'batteri' i navnet på oppslag: $delerMedBatteriINavn" }
        }

        val brukersKommunenummer = brukersKommunenummerResult.await() ?: return@coroutineScope OppslagResultat(null, OppslagFeil.INGET_UTLÅN, HttpStatusCode.NotFound)
        val lagerstatusForDeler =
            oebs.hentLagerstatusForKommunenummer(brukersKommunenummer, hjelpemiddelMedDeler.deler.map { it.hmsnr })

        // Koble hver del til lagerstatus, og sorter på navn
        hjelpemiddelMedDeler.deler =
            hjelpemiddelMedDeler.deler.map { del ->
                val lagerstatus = lagerstatusForDeler.find { it.artikkelnummer == del.hmsnr }
                if (isDev()) {
                    log.info { "Lagerstatus for ${del.hmsnr}: $lagerstatus" }
                }
                del.copy(lagerstatus = lagerstatus)
            }.sortedBy { it.navn }

        val sentral = hjelpemiddelMedDeler.deler.first().lagerstatus?.organisasjons_navn ?: "UKJENT"
        val antallPåLager = hjelpemiddelMedDeler.deler.count { it.lagerstatus?.minmax == true }
        val antallDeler = hjelpemiddelMedDeler.deler.count()
        log.info { "Lagerstatus for $hmsnr hos $sentral: $antallPåLager av $antallDeler er på lager." }
        if (antallPåLager < antallDeler) {
            val ikkePåLager = hjelpemiddelMedDeler.deler.filter { it.lagerstatus?.minmax == false }.map { it.hmsnr }
            val manglerLagerstatus = hjelpemiddelMedDeler.deler.filter { it.lagerstatus == null }.map { it.hmsnr }
            log.info { "$sentral har ikke alle deler på lager for $hmsnr. Ikke på lager: $ikkePåLager, mangler lagerstatus: $manglerLagerstatus." }
        }

        log.info { "Antall deler for hmsnr $hmsnr: ${hjelpemiddelMedDeler.deler.size}, antall unike kategorier: ${hjelpemiddelMedDeler.antallKategorier()}" }
        metrics.antallKategorier(hjelpemiddelMedDeler.antallKategorier())

        val antallDelerTilgjengeligMenIkkePåMinmax =
            hjelpemiddelMedDeler.deler.count { it.lagerstatus != null && !it.lagerstatus!!.minmax && it.lagerstatus!!.tilgjengelig > 0 }
        if (antallDelerTilgjengeligMenIkkePåMinmax > 0) {
            log.info { "Antall tilgjengelig deler (ikke minmax) for $hmsnr: $antallDelerTilgjengeligMenIkkePåMinmax" }
        }

        OppslagResultat(
            hjelpemiddelMedDeler,
            null,
            HttpStatusCode.OK,
            piloter = piloterService.hentPiloter(brukersKommunenummer)
        )
    }

    private fun sjekkOmGrunndataDekkerManuellListeForHjm(
        manuell: HjelpemiddelMedDeler?,
        grunndata: HjelpemiddelMedDeler?
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
}