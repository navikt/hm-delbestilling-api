package no.nav.hjelpemidler.delbestilling.fakes

import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.OebsSink
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Ordre
import no.nav.hjelpemidler.delbestilling.testdata.FakeOebsLager

class OebsSinkFake(
    private val lager: FakeOebsLager
): OebsSink {

    private val ordrer = mutableListOf<Ordre>()

    fun sisteOrdre() = ordrer.last()

    override fun sendDelbestilling(ordre: Ordre) {
        ordrer.add(ordre)
        ordre.artikler.forEach {
            lager.reduser(it.hmsnr, it.antall)
        }
    }
}