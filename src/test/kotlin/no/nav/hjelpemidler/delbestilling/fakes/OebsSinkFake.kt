package no.nav.hjelpemidler.delbestilling.fakes

import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.OebsSink
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Ordre
import no.nav.hjelpemidler.delbestilling.testdata.FakeOebsLager

class OebsSinkFake(
    private val lager: FakeOebsLager
): OebsSink {

    private val ordrer = mutableListOf<Ordre>()

    var skalKasteFeil: Boolean = false
    var feil: Throwable = RuntimeException("Fake feil")

    override fun sendDelbestilling(ordre: Ordre) {
        if (skalKasteFeil) {
            throw feil
        }

        ordrer.add(ordre)
        ordre.artikler.forEach {
            lager.reduser(it.hmsnr, it.antall)
        }
    }
}