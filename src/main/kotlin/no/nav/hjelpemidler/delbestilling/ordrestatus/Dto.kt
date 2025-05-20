package no.nav.hjelpemidler.delbestilling.ordrestatus

import no.nav.hjelpemidler.delbestilling.common.DellinjeStatus
import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.common.Status
import java.time.LocalDate

data class StatusOppdateringRequest(
    val status: Status,
    val oebsOrdrenummer: String,
)

data class DellinjeStatusOppdateringRequest(
    val status: DellinjeStatus,
    val hmsnr: Hmsnr,
    val datoOppdatert: LocalDate,
)