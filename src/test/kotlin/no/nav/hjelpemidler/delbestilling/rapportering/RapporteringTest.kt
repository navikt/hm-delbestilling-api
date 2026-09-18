package no.nav.hjelpemidler.delbestilling.rapportering

import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.ANMODNINGSBEHOV_SUBJECT
import no.nav.hjelpemidler.delbestilling.runWithTestContext
import no.nav.hjelpemidler.delbestilling.testdata.Testdata.delPåLager
import no.nav.hjelpemidler.delbestilling.testdata.fixtures.gittDelbestillingUtenLagerdekning
import no.nav.hjelpemidler.delbestilling.testdata.fixtures.opprettDelbestillingMedDel
import kotlin.test.Test
import kotlin.test.assertTrue

class RapporteringTest {

    @Test
    fun `skal sende ut e-post om anmodningsbehov når det er bestilt deler uten lagerdekning`() =
        runWithTestContext {
            gittDelbestillingUtenLagerdekning()

            rapportering.rapporterAnmodningsbehov()

            assertTrue(emailClient.outbox.any { it.subject.contains(ANMODNINGSBEHOV_SUBJECT) })
        }

    @Test
    fun `skal ikke sende ut e-post om anmodningsbehov når det er bestilt deler med lagerdekning`() =
        runWithTestContext {
            opprettDelbestillingMedDel(delPåLager)

            rapportering.rapporterAnmodningsbehov()

            assertTrue(emailClient.outbox.none { it.subject.contains(ANMODNINGSBEHOV_SUBJECT) })
        }
}
