package no.nav.hjelpemidler.delbestilling.hjelpemidler

import io.ktor.http.*
import no.nav.hjelpemidler.delbestilling.delbestilling.AlleHjelpemidlerMedDelerResultat

class HjelpemidlerService {


    suspend fun hentAlleHjelpemidlerMedDeler(): AlleHjelpemidlerMedDelerResultat {
        val alleHjelpemidlerMedDeler = HjelpemiddelDeler.hentAlleHjelpemidlerMedDeler()
        return AlleHjelpemidlerMedDelerResultat(alleHjelpemidlerMedDeler, HttpStatusCode.OK)
    }

}