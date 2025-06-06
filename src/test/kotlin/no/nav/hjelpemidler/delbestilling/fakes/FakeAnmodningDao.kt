package no.nav.hjelpemidler.delbestilling.fakes

import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.AnmodningDao
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.Anmodningrapport

class FakeAnmodningDao : AnmodningDao {


    private val anmodninger = mutableListOf<Anmodningrapport>()


    override fun lagreAnmodninger(rapport: Anmodningrapport) {
        anmodninger += rapport
    }

}