package no.nav.hjelpemidler.delbestilling.testdata.fixtures

import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.common.Lager
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.Del
import no.nav.hjelpemidler.delbestilling.testdata.TestContext
import no.nav.hjelpemidler.delbestilling.testdata.Testdata
import no.nav.hjelpemidler.domain.enhet.Enhet

suspend fun TestContext.hentDelerUtenDekning(
    enhet: Lager = Testdata.defaultEnhet,
): List<Del> {
    return transaction {
        delUtenDekningDao
            .hentDelerTilRapportering(enhet.nummer)
    }
}

suspend fun TestContext.hentDelUtenDekning(
    hmsnr: Hmsnr = Testdata.defaultDelHmsnr,
): Del {
    return hentDelerUtenDekning().find { it.hmsnr == hmsnr }!!
}
