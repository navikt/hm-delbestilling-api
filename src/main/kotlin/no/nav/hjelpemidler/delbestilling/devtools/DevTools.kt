package no.nav.hjelpemidler.delbestilling.devtools

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.common.Lagerstatus
import no.nav.hjelpemidler.delbestilling.config.isDev
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.DelUtenDekningDao
import no.nav.hjelpemidler.delbestilling.infrastructure.email.ContentType
import no.nav.hjelpemidler.delbestilling.infrastructure.email.Email
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Oebs
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.Pdl
import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction.Transactional
import no.nav.hjelpemidler.delbestilling.oppslag.FinnDelerTilHjelpemiddel
import no.nav.hjelpemidler.delbestilling.oppslag.OppslagResultat
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.data.hmsnr2Hjm


private val log = KotlinLogging.logger { }

class DevTools(
    private val transaction: Transactional,
    private val oebs: Oebs,
    private val pdl: Pdl,
    private val finnDelerTilHjelpemiddel: FinnDelerTilHjelpemiddel,
    private val email: Email
) {

    init {
        check(isDev()) { "DevTools skal kun brukes i dev-miljøet" }
    }

    suspend fun finnTestpersonMedTestbartUtlån(): Map<String, String> {
        val fnrCache = mutableSetOf<String>()
        hmsnr2Hjm.keys.forEach { artnr ->
            log.info { "Leter etter testpersoner med utlån på $artnr" }
            val utlån = oebs.hentUtlånPåArtnr(artnr)
            utlån.forEach { (fnr, artnr, serienr, utlånsDato) ->
                try {
                    if (fnr !in fnrCache) {
                        val kommunenr = pdl.hentKommunenummer(fnr)
                        log.info { "Fant testperson $fnr med utlån på $artnr, $serienr i kommune $kommunenr" }
                        return mapOf("fnr" to fnr, "artnr" to artnr, "serienr" to serienr, "kommunenr" to kommunenr)
                    }
                } catch (e: Exception) {
                    // Peronen finnes ikke i PDL. Ignorer og let videre.
                    log.info(e) { "Ignorer PDL feil under scanning etter testperson" }
                    fnrCache.add(fnr)
                }
            }
        }
        return mapOf("error" to "Ingen testperson funnet")
    }

    suspend fun slåOppHjelpemiddelMedFakeLagerstatus(hmsnr: String, serienr: String): OppslagResultat {
        log.info { "Slår opp hmsnr=$hmsnr for dev.ekstern, og beriker med fake lagerstatus" }
        var hjelpemiddel = finnDelerTilHjelpemiddel(hmsnr).sorterDeler()

        // legg på pseudo-random lagerstatus
        val delerMedLagerstatus = hjelpemiddel.deler.map { del ->
            val erMinmax = del.hmsnr.toInt() % 3 != 0                // Gjør ca 66% tilgjengelig
            val antallPåLager = del.hmsnr.takeLast(1).toInt()    // Antall tilgjengelig = siste siffer i hmsnr
            del.copy(
                lagerstatus = Lagerstatus(
                    organisasjons_id = 292,
                    organisasjons_navn = "*19 Troms",
                    artikkelnummer = del.hmsnr,
                    minmax = erMinmax,
                    tilgjengelig = antallPåLager,
                    antallDelerPåLager = antallPåLager
                )
            )
        }

        if (hjelpemiddel.harBatteri()) {
            hjelpemiddel = hjelpemiddel.copy(antallDagerSidenSistBatteribestilling = serienr.take(3).toInt())
        }

        hjelpemiddel = hjelpemiddel.copy(
            deler = delerMedLagerstatus,
            erInnenforGaranti = when (hjelpemiddel.hmsnr) {
                "238378" -> true // For testing: Comet Alpine Plus
                else -> false
            },
        )

        return OppslagResultat(hjelpemiddel)
    }

    suspend fun markerDelerSomIkkeBehandlet() = transaction {
        delUtenDekningDao.markerDelerSomIkkeBehandlet()
    }

    private fun DelUtenDekningDao.markerDelerSomIkkeBehandlet() {
        tx.update(
            sql = """
                UPDATE deler_uten_dekning
                SET behandlet_tidspunkt = NULL, 
                    status='AVVENTER'
            """.trimIndent()
        )
    }

    suspend fun sendTestMail() = email.sendTestMail()

    private suspend fun Email.sendTestMail(
        recipentEmail: String = "digitalisering.av.hjelpemidler.og.tilrettelegging@nav.no",
        subject: String = "[TEST] hm-delbestilling-api",
        bodyText: String = "Dette er bare en test av epostutsending fra hm-delbestilling-api. Vennligst ignorer meg.",
    ) {
        client.sendEmail(
            recipentEmail = recipentEmail,
            subject = subject,
            bodyText = bodyText,
            contentType = ContentType.TEXT
        )
        log.info { "post til $recipentEmail sendt." }
    }

}

