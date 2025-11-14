package no.nav.hjelpemidler.delbestilling.infrastructure.leaderElection

import no.nav.hjelpemidler.delbestilling.runWithTestContext
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class ErLederTest {

    @Test
    fun `skal erklære denne instansen som leder dersom den har samme hostname som leder`() = runWithTestContext {
        localHost.hostname = "localhost"
        elector.leder = "localhost"

        assertTrue(erLeder())
    }

    @Test
    fun `skal ignorere casing ved sjekk av leder`() = runWithTestContext {
        localHost.hostname = "LOCALHOST"
        elector.leder = "localhost"

        assertTrue(erLeder())
    }

    @Test
    fun `skal ikke være leder dersom hostname ikke matcher leders hostname`() = runWithTestContext {
        localHost.hostname = "hm-delbestilling-api-6847cb987f-qdhrn"
        elector.leder = "hm-delbestilling-api-bcf975f9d-tqvxx"

        assertFalse(erLeder())
    }

    @Test
    fun `skal ikke være leder dersom henting av instansen sitt hostname feiler`() = runWithTestContext {
        localHost.hostname = null
        elector.leder = "localhost"

        assertFalse(erLeder())
    }

}