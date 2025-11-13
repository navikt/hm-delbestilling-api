package no.nav.hjelpemidler.delbestilling.testdata

import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.LagerstatusResponse

class FakeOebsLager {
    private val store: MutableMap<Hmsnr, LagerstatusResponse?> = mutableMapOf()

    init {
        set(Testdata.delPåMinmax, antall = 5, minmax = true)
        set(Testdata.delPåLager, antall = 5, minmax = false)
    }

    fun tømLager() {
        store.clear()
    }

    fun set(hmsnr: Hmsnr, antall: Int, minmax: Boolean = true, orgId: Int = 243, orgNavn: String = "*03 Oslo") {
        set(
            LagerstatusResponse(
                antallPåLager = antall,
                organisasjons_id = orgId,
                organisasjons_navn = orgNavn,
                artikkelnummer = hmsnr,
                tilgjengelig = antall,
                minmax = minmax,
            )
        )
    }

    fun set(lagerstatusResponse: LagerstatusResponse) {
        store[lagerstatusResponse.artikkelnummer] = lagerstatusResponse
    }

    fun setNull(hmsnr: Hmsnr) {
        store[hmsnr] = null
    }

    fun hent(hmsnr: Hmsnr): LagerstatusResponse? {
        if (hmsnr !in store) {
            set(hmsnr, antall = 0, minmax = false)
        }
        return store[hmsnr]
    }

    fun reduser(hmsnr: Hmsnr, antall: Int) {
        val prev = hent(hmsnr) ?: error("Lagerstatus er ikke satt for hmsnr $hmsnr")
        val nyttAntall = prev.antallPåLager - antall
        set(prev.copy(antallPåLager = nyttAntall, tilgjengelig = nyttAntall))
    }
}
