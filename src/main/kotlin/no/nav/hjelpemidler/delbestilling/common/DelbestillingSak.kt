package no.nav.hjelpemidler.delbestilling.common

import java.time.LocalDate
import java.time.LocalDateTime

data class DelbestillingSak(
    val saksnummer: Long,
    val delbestilling: Delbestilling,
    val opprettet: LocalDateTime,
    val status: Status,
    val sistOppdatert: LocalDateTime,
    val oebsOrdrenummer: String?,
    val brukersKommunenummer: String,
    val brukersKommunenavn: String,
    val enhetnr: String,
    val enhetnavn: String,
) {
    fun oppdaterOebsOrdrenummer(ordrenummer: String): DelbestillingSak {
        if (this.oebsOrdrenummer == null) {
            return this.copy(oebsOrdrenummer = ordrenummer)
        } else {
            require(this.oebsOrdrenummer == ordrenummer) { "Mismatch i oebsOrdrenummer for delbestilling $saksnummer. Eksisterende oebsOrdrenummer: $oebsOrdrenummer. Mottatt oebsOrdrenummer: $ordrenummer" }
            return this
        }
    }

    fun oppdaterStatus(status: Status): DelbestillingSak {
        if (this.status.ordinal < status.ordinal) {
            return this.copy(status = status)
        }
        return this
    }

    fun oppdaterDellinjeStatus(status: DellinjeStatus, hmsnr: Hmsnr, datoOppdatert: LocalDate): DelbestillingSak {
        val oppdatertDelbestilling = delbestilling.oppdaterDellinjeStatus(status, hmsnr, datoOppdatert)
        val nyStatus = when(oppdatertDelbestilling.alleDelerErSkipningsbekreftet()) {
            true -> Status.SKIPNINGSBEKREFTET
            false -> Status.DELVIS_SKIPNINGSBEKREFTET
        }
        return copy(delbestilling = oppdatertDelbestilling).oppdaterStatus(nyStatus)
    }
}