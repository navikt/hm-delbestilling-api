package no.nav.hjelpemidler.delbestilling.testdata.fixtures

import no.nav.hjelpemidler.delbestilling.common.DelbestillingSak
import no.nav.hjelpemidler.delbestilling.testdata.TestContext
import no.nav.hjelpemidler.delbestilling.testdata.Testdata

suspend fun TestContext.hentDelbestilling(
    oebsOrdrenummer: String,
): DelbestillingSak? {
    return transaction {
        delbestillingRepository.hentDelbestilling(oebsOrdrenummer)
    }
}

suspend fun TestContext.hentDelbestilling(
    saksnummer: Long,
): DelbestillingSak {
    return transaction {
        delbestillingRepository.hentDelbestilling(saksnummer)
    }!!
}

suspend fun TestContext.hentDelbestillinger(
    fnr: String = Testdata.defaultFnr,
): List<DelbestillingSak> {
    return delbestillingService.hentDelbestillinger(fnr)
}