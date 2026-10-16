package no.nav.hjelpemidler.delbestilling.fakes

import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Brukerpass
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.LagerstatusResponse
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.OebsApiProxy
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.OebsPersoninfo
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Utlån
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.UtlånMedSerienr
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.UtlånMedSerienrResponse
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.UtlånResponse
import no.nav.hjelpemidler.delbestilling.testdata.FakeOebsLager
import no.nav.hjelpemidler.delbestilling.testdata.DEFAULT_BRUKERNRSTYRT_HJM_FNR
import no.nav.hjelpemidler.delbestilling.testdata.DEFAULT_BRUKERNRSTYRT_HJM_HMSNR
import no.nav.hjelpemidler.delbestilling.testdata.DEFAULT_HJM_HMSNR
import no.nav.hjelpemidler.delbestilling.testdata.DEFAULT_HJM_SERIENR
import no.nav.hjelpemidler.delbestilling.testdata.DEFAULT_KOMMUNENUMMER
import no.nav.hjelpemidler.delbestilling.testdata.FNR_INNBYGGER
import no.nav.hjelpemidler.domain.person.Fødselsnummer
import no.nav.hjelpemidler.domain.person.år
import java.time.LocalDate

class OebsApiProxyFake(
    private val lager: FakeOebsLager
) : OebsApiProxy {

    var utlånMedSerienr: UtlånMedSerienr? = UtlånMedSerienr(
        fnr = FNR_INNBYGGER,
        artnr = DEFAULT_HJM_HMSNR,
        serienr = DEFAULT_HJM_SERIENR,
        opprettetDato = LocalDate.of(2025, 1, 1),
        utlånsDato = "2025-02-03",
        isokode = "123456"
    )

    var utlånMedBrukernr: List<Utlån> = listOf(
        Utlån(
            fnr = DEFAULT_BRUKERNRSTYRT_HJM_FNR,
            artnr = DEFAULT_BRUKERNRSTYRT_HJM_HMSNR,
            serienr = null,
            opprettetDato = LocalDate.of(2025, 1, 1),
            utlånsDato = "2025-02-03",
            isokode = "123456"
        )
    )

    var personinfo = listOf(OebsPersoninfo(DEFAULT_KOMMUNENUMMER))

    override suspend fun hentUtlånPåArtnrOgSerienr(artnr: String, serienr: String): UtlånMedSerienrResponse {
        return UtlånMedSerienrResponse(utlånMedSerienr)
    }

    override suspend fun hentUtlånPåArtnrOgBrukernr(artnr: String, brukernr: String): UtlånResponse {
        return UtlånResponse(utlånMedBrukernr)
    }

    override suspend fun hentUtlånPåArtnr(artnr: String): List<UtlånMedSerienr> {
        error("hentUtlånPåArtnr er ikke implementert")
    }

    override suspend fun hentPersoninfo(fnr: String): List<OebsPersoninfo> {
        return personinfo
    }

    override suspend fun hentBrukerpassinfo(fnr: String): Brukerpass {
        return Brukerpass(true)
    }

    override suspend fun hentLagerstatusForEnhetnr(enhetnr: String, hmsnrs: List<String>): List<LagerstatusResponse> {
        return hmsnrs.mapNotNull { lager.hent(it) }
    }

    override suspend fun hentFnr(brukernr: String): Fødselsnummer {
        return Fødselsnummer(40.år)
    }

}