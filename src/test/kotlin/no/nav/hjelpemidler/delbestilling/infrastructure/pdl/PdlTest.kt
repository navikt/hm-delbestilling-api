package no.nav.hjelpemidler.delbestilling.infrastructure.pdl

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.hjelpemidler.delbestilling.infrastructure.jsonMapper
import no.nav.hjelpemidler.delbestilling.testdata.PdlRespons
import no.nav.hjelpemidler.delbestilling.testdata.Testdata
import no.nav.hjelpemidler.delbestilling.testdata.runWithTestContext
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

internal class PdlTest {

    @Test
    fun `skal hente brukers kommunenr`() = runWithTestContext {
        pdlClient.response = PdlRespons.person(kommunenummer = Testdata.defaultKommunenummer)
        val kommunenummer = pdl.hentKommunenummer(Testdata.fnr)
        assertEquals(Testdata.defaultKommunenummer, kommunenummer)
    }

    @Test
    fun `skal kaste feil hvis komunenummer mangler`() = runWithTestContext {
        pdlClient.response = PdlRespons.person(kommunenummer = null)
        val exception = assertFailsWith<PdlResponseMissingData> {
            pdl.hentKommunenummer(Testdata.fnr)
        }
        assertTrue(exception.message!!.contains("Kommunenummer mangler i PDL-data"))
    }

    @Test
    fun `skal hente ut fornavn`() = runWithTestContext {
        pdlClient.response = PdlRespons.person("Arne", "Bjarne", "Charlie")
        assertEquals("Arne", pdl.hentFornavn(Testdata.fnr))
    }

    @Test
    fun `skal kaste feil hvis fornavn mangler`() = runWithTestContext {
        pdlClient.response = PdlPersonResponse(data = PdlHentPerson(null))
        val exception = assertFailsWith<PdlResponseMissingData> {
            pdl.hentFornavn(Testdata.fnr)
        }
        assertTrue(exception.message!!.contains("Fornavn mangler i PDL-data"))
    }

    @Test
    fun `skal kaste feil dersom person ikke finnes i PDL`() = runWithTestContext {
        pdlClient.response = PdlRespons.personIkkeFunnet()
        assertFailsWith<PersonNotFoundInPdl> { pdl.hentKommunenummer(Testdata.fnr) }
    }

    @Test
    fun `skal kaste feil dersom person har gradering STRENGT_FORTROLIG (kode 6) `() = runWithTestContext {
        pdlClient.response = PdlRespons.kode6()
        assertFailsWith<PersonNotAccessibleInPdl> { pdl.hentKommunenummer(Testdata.fnr) }
    }

    @Test
    fun `skal kaste feil dersom person har gradering FORTROLIG (kode 7) `() = runWithTestContext {
        pdlClient.response = PdlRespons.kode7()
        assertFailsWith<PersonNotAccessibleInPdl> { pdl.hentFornavn(Testdata.fnr) }
    }
}

private fun pdlPersonResponse(json: String): PdlPersonResponse = jsonMapper.readValue(json)
