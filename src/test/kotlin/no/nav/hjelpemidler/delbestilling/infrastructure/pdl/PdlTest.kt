package no.nav.hjelpemidler.delbestilling.infrastructure.pdl

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import no.nav.hjelpemidler.delbestilling.infrastructure.monitoring.PdlResponseMissingData
import no.nav.hjelpemidler.delbestilling.infrastructure.monitoring.PersonNotAccessibleInPdl
import no.nav.hjelpemidler.delbestilling.infrastructure.monitoring.PersonNotFoundInPdl
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class PdlTest {

    private val client = mockk<PdlClient>()
    private val pdl = Pdl(client)

    private val fnr = "00000000000"
    private val kommunenummer = "3801"


    @Test
    fun `hent brukers kommunenr`() = runTest {
        coEvery { client.hentKommunenummer(fnr) } returns pdlPerson(kommunenummer = kommunenummer)
        assertEquals(kommunenummer, pdl.hentKommunenummer(fnr))
    }

    @Test
    fun `skal kaste feil hvis komunenummer mangler`() = runTest {
        coEvery { client.hentKommunenummer(fnr) } returns PdlPersonResponse(PdlHentPerson(null))
        assertThrows<PdlResponseMissingData> { pdl.hentKommunenummer(fnr) }
    }

    @Test
    fun `skal hente ut fornavn`() = runTest {
        coEvery { client.hentPersonNavn(fnr) } returns pdlPerson("Arne", "Bjarne", "Charlie")
        assertEquals("Arne", pdl.hentFornavn(fnr))
    }

    @Test
    fun `skal kaste feil hvis fornavn mangler`() = runTest {
        coEvery { client.hentPersonNavn(fnr) } returns PdlPersonResponse(PdlHentPerson(null))
        assertThrows<PdlResponseMissingData> { pdl.hentFornavn(fnr) }
    }

    @Test
    fun `skal kaste feil dersom person ikke finnes i PDL`() = runTest {
        val pdlClient = pdlClientWithResponse(
            pdlPersonIkkeFunnet()
        )
        assertThrows<PersonNotFoundInPdl> { pdlClient.hentKommunenummer(fnr) }
    }

    @Test
    fun `skal kaste feil dersom person har gradering STRENGT_FORTROLIG (kode 6) `() = runTest {
        val pdlClient = pdlClientWithResponse(
            pdlKode6Respons(kommunenr)
        )
        assertThrows<PersonNotAccessibleInPdl> { pdlClient.hentKommunenummer(fnr) }
    }

    @Test
    fun `skal kaste feil dersom person har gradering fortrolig (kode 7) `() = runTest {
        val pdlClient = pdlClientWithResponse(
            pdlKode7Respons(kommunenr)
        )
        assertThrows<PersonNotAccessibleInPdl> { pdlClient.hentPersonNavn(fnr) }
    }
}

