package no.nav.hjelpemidler.delbestilling.infrastructure.pdl

import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import no.nav.hjelpemidler.delbestilling.infrastructure.jsonMapper
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
        coEvery { client.hentKommunenummer(fnr) } returns pdlPersonResponse(pdlPersonIkkeFunnet())
        assertThrows<PersonNotFoundInPdl> { pdl.hentKommunenummer(fnr) }
    }

    @Test
    fun `skal kaste feil dersom person har gradering STRENGT_FORTROLIG (kode 6) `() = runTest {
        coEvery { client.hentKommunenummer(fnr) } returns pdlPersonResponse(pdlKode6Respons(kommunenummer))
        assertThrows<PersonNotAccessibleInPdl> { pdl.hentKommunenummer(fnr) }
    }

    @Test
    fun `skal kaste feil dersom person har gradering FORTROLIG (kode 7) `() = runTest {
        coEvery { client.hentPersonNavn(fnr) } returns pdlPersonResponse(pdlKode7Respons(kommunenummer))
        assertThrows<PersonNotAccessibleInPdl> { pdl.hentFornavn(fnr) }
    }
}

private fun pdlPersonResponse(json: String): PdlPersonResponse = jsonMapper.readValue(json)
