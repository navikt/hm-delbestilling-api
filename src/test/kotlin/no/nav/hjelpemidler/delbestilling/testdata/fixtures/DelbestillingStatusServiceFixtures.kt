package no.nav.hjelpemidler.delbestilling.testdata.fixtures

import no.nav.hjelpemidler.delbestilling.common.DellinjeStatus
import no.nav.hjelpemidler.delbestilling.common.Status
import no.nav.hjelpemidler.delbestilling.testdata.TestContext
import java.time.LocalDate

suspend fun TestContext.annullerSak(saksnummer: Long) {
    delbestillingStatusService.oppdaterStatus(saksnummer, Status.ANNULLERT, "123")
}

suspend fun TestContext.oppdaterDellinjeStatus(
    oebsOrdrenummer: String,
) {
    delbestillingStatusService.oppdaterDellinjeStatus(
        oebsOrdrenummer,
        DellinjeStatus.SKIPNINGSBEKREFTET,
        "123456",
        LocalDate.now(),
    )
}
