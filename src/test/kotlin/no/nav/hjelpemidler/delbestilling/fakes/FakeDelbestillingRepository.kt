package no.nav.hjelpemidler.delbestilling.fakes

import no.nav.hjelpemidler.delbestilling.common.Delbestilling
import no.nav.hjelpemidler.delbestilling.common.DelbestillingSak
import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.common.Serienr
import no.nav.hjelpemidler.delbestilling.common.Status
import no.nav.hjelpemidler.delbestilling.delbestilling.BestillerType
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingRepository
import no.nav.hjelpemidler.delbestilling.infrastructure.roller.Organisasjon
import no.nav.hjelpemidler.delbestilling.testdata.Testdata
import no.nav.hjelpemidler.delbestilling.testdata.organisasjon
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicLong

class FakeDelbestillingRepository : DelbestillingRepository {

    private val saksnummerCounter = AtomicLong(0L)
    private val sakStore = mutableMapOf<Long, DelbestillingSak>()
    private val bestillerTilSaksnummer = mutableMapOf<String, MutableSet<Long>>()

    fun insert(sak: DelbestillingSak, bestillerFnr: String = Testdata.defaultFnr): Long {
        val saksnummer = if (sak.saksnummer > 0) sak.saksnummer else saksnummerCounter.incrementAndGet()
        sakStore[saksnummer] = sak
        bestillerTilSaksnummer.getOrPut(bestillerFnr) { mutableSetOf() }.add(saksnummer)
        return saksnummer
    }

    override fun lagreDelbestilling(
        bestillerFnr: String,
        brukerFnr: String,
        brukerKommunenr: String,
        delbestilling: Delbestilling,
        brukersKommunenavn: String,
        bestillersOrganisasjon: Organisasjon,
        bestillerType: BestillerType
    ): Long {
        val now = LocalDateTime.now()
        val sak = DelbestillingSak(
            saksnummer = -1,
            delbestilling = delbestilling,
            opprettet = now,
            status = Status.INNSENDT,
            sistOppdatert = now,
            oebsOrdrenummer = null,
            brukersKommunenummer = brukerKommunenr,
            brukersKommunenavn = brukersKommunenavn
        )
        return insert(sak, bestillerFnr)
    }

    override fun hentDelbestillinger(bestillerFnr: String): List<DelbestillingSak> =
        bestillerTilSaksnummer[bestillerFnr]?.mapNotNull { sakStore[it] } ?: emptyList()


    override fun hentDelbestillinger(hmsnr: Hmsnr, serienr: Serienr): List<DelbestillingSak> =
        sakStore.values.filter { it.delbestilling.hmsnr == hmsnr && it.delbestilling.serienr == serienr }


    override fun hentDelbestillingerForKommune(brukerKommunenr: String): List<DelbestillingSak> =
        sakStore.values.filter { it.brukersKommunenummer == brukerKommunenr }


    override fun hentDelbestilling(saksnummer: Long): DelbestillingSak? =
        sakStore[saksnummer]


    override fun hentDelbestilling(oebsOrdrenummer: String): DelbestillingSak? =
        sakStore.values.find { it.oebsOrdrenummer == oebsOrdrenummer }


    override fun oppdaterDelbestillingSak(sak: DelbestillingSak) {
        sakStore[sak.saksnummer] = sak
    }
}