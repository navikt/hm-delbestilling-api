package no.nav.hjelpemidler.delbestilling.infrastructure.oebs

import no.nav.hjelpemidler.delbestilling.common.Levering
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class GenererForsendelsesinfoTest {

    @Test
    fun `forsendelsesinfo skal inneholde 'XK-Lager' ved levering til XK-Lager`() {
        assertEquals(
            "XK-Lager Del bestilt av: Turid Tekniker",
            genererForsendelsesinfo(Levering.TIL_XK_LAGER, "Turid Tekniker")
        )

        assertEquals(
            "Del bestilt av: Lars",
            genererForsendelsesinfo(Levering.TIL_SERVICE_OPPDRAG, "Lars")
        )
    }
}