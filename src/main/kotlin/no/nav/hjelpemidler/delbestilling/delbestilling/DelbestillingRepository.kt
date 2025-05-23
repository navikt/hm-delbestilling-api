package no.nav.hjelpemidler.delbestilling.delbestilling

import no.nav.hjelpemidler.database.UpdateResult
import no.nav.hjelpemidler.delbestilling.common.Delbestilling
import no.nav.hjelpemidler.delbestilling.common.DelbestillingSak
import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.common.Serienr
import no.nav.hjelpemidler.delbestilling.infrastructure.roller.Organisasjon

interface DelbestillingRepository {
    fun lagreDelbestilling(
        bestillerFnr: String,
        brukerFnr: String,
        brukerKommunenr: String,
        delbestilling: Delbestilling,
        brukersKommunenavn: String,
        bestillersOrganisasjon: Organisasjon,
        bestillerType: BestillerType,
    ): Long

    fun hentDelbestillinger(bestillerFnr: String): List<DelbestillingSak>
    fun hentDelbestillingerForKommune(brukerKommunenr: String): List<DelbestillingSak>
    fun hentDelbestillinger(hmsnr: Hmsnr, serienr: Serienr): List<DelbestillingSak>
    fun hentDelbestilling(saksnummer: Long): DelbestillingSak?
    fun hentDelbestilling(oebsOrdrenummer: String): DelbestillingSak?
    fun oppdaterDelbestillingSak(sak: DelbestillingSak)
}