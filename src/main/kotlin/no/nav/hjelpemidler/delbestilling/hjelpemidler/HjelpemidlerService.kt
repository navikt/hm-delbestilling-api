package no.nav.hjelpemidler.delbestilling.hjelpemidler

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import no.nav.hjelpemidler.delbestilling.delbestilling.AlleHjelpemidlerMedDelerResultat
import no.nav.hjelpemidler.delbestilling.delbestilling.HjelpemiddelMedDeler
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hmsnr2Hjm

private val logger = KotlinLogging.logger { }

class HjelpemidlerService {

    fun hentAlleHjelpemidlerMedDeler(): AlleHjelpemidlerMedDelerResultat {
        val alleHjelpemidlerMedDeler = hmsnr2Hjm.values.toList()
        return AlleHjelpemidlerMedDelerResultat(alleHjelpemidlerMedDeler, HttpStatusCode.OK)
    }

    fun hentHjelpemiddelMedDeler(hmsnrHjelpemiddel: String): HjelpemiddelMedDeler? {
        val hjelpemiddel = hmsnr2Hjm[hmsnrHjelpemiddel]
        logger.info { "Fant $hjelpemiddel for $hmsnrHjelpemiddel" }
        return hjelpemiddel
    }

}