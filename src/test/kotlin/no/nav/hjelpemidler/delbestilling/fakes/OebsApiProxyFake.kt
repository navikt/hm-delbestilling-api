package no.nav.hjelpemidler.delbestilling.fakes

import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Brukerpass
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.LagerstatusResponse
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.OebsApiProxy
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.OebsPersoninfo
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Utlån
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.UtlånPåArtnrOgSerienrResponse
import no.nav.hjelpemidler.delbestilling.testdata.FakeOebsLager
import no.nav.hjelpemidler.delbestilling.testdata.Testdata
import no.nav.hjelpemidler.delbestilling.testdata.Testdata.fnr
import no.nav.hjelpemidler.delbestilling.testdata.Testdata.defaultHjmHmsnr
import no.nav.hjelpemidler.delbestilling.testdata.Testdata.defaultHjmSerienr
import java.time.LocalDate

class OebsApiProxyFake(
    private val lager: FakeOebsLager
) : OebsApiProxy {

    var utlån: Utlån? = Utlån(
        fnr = fnr,
        artnr = defaultHjmHmsnr,
        serienr = defaultHjmSerienr,
        opprettetDato = LocalDate.of(2025, 1, 1),
        utlånsDato = "2025-02-03",
        isokode = "123456"
    )

    var personinfo = listOf(OebsPersoninfo(Testdata.defaultKommunenummer))

    override suspend fun hentUtlånPåArtnrOgSerienr(artnr: String, serienr: String): UtlånPåArtnrOgSerienrResponse {
        return UtlånPåArtnrOgSerienrResponse(utlån)
    }

    override suspend fun hentUtlånPåArtnr(artnr: String): List<Utlån> {
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

}