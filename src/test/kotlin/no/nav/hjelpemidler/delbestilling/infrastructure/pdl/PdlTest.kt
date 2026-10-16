package no.nav.hjelpemidler.delbestilling.infrastructure.pdl

import no.nav.hjelpemidler.delbestilling.infrastructure.jsonMapper
import no.nav.hjelpemidler.delbestilling.testdata.DEFAULT_KOMMUNENUMMER
import no.nav.hjelpemidler.delbestilling.testdata.FNR_INNBYGGER
import no.nav.hjelpemidler.delbestilling.runWithTestContext
import org.junit.jupiter.api.Test
import tools.jackson.module.kotlin.readValue
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

internal class PdlTest {

    @Test
    fun `skal hente brukers kommunenr`() = runWithTestContext {
        pdlClient.person(FNR_INNBYGGER, kommunenummer = DEFAULT_KOMMUNENUMMER)
        val kommunenummer = pdl.hentKommunenummer(FNR_INNBYGGER)
        assertEquals(DEFAULT_KOMMUNENUMMER, kommunenummer)
    }

    @Test
    fun `skal kaste feil hvis komunenummer mangler`() = runWithTestContext {
        pdlClient.person(FNR_INNBYGGER, kommunenummer = null)
        val exception = assertFailsWith<PdlResponseMissingData> {
            pdl.hentKommunenummer(FNR_INNBYGGER)
        }
        assertTrue(exception.message!!.contains("Kommunenummer mangler i PDL-data"))
    }

    @Test
    fun `skal hente ut fornavn`() = runWithTestContext {
        pdlClient.person(FNR_INNBYGGER, fornavn = "Arne", etternavn = "Bjarne", mellomnavn = "Charlie")
        assertEquals("Arne", pdl.hentFornavn(FNR_INNBYGGER))
    }

    @Test
    fun `skal hente ut fullt navn med og uten mellomnavn`() = runWithTestContext {
        pdlClient.person(FNR_INNBYGGER, fornavn = "Arne", etternavn = "Charlie", mellomnavn = "Bjarne")
        assertEquals("Arne Bjarne Charlie", pdl.hentNavn(FNR_INNBYGGER))

        pdlClient.person(FNR_INNBYGGER, fornavn = "Arne", etternavn = "Charlie")
        assertEquals("Arne Charlie", pdl.hentNavn(FNR_INNBYGGER))
    }

    @Test
    fun `skal kaste feil hvis fornavn mangler`() = runWithTestContext {
        pdlClient.svar(FNR_INNBYGGER, PdlPersonResponse(data = PdlHentPerson(null)))
        val exception = assertFailsWith<PdlResponseMissingData> {
            pdl.hentFornavn(FNR_INNBYGGER)
        }
        assertTrue(exception.message!!.contains("Fornavn mangler i PDL-data"))
    }

    @Test
    fun `skal kaste feil dersom person ikke finnes i PDL`() = runWithTestContext {
        pdlClient.ikkeFunnet(FNR_INNBYGGER)
        assertFailsWith<PersonNotFoundInPdl> { pdl.hentKommunenummer(FNR_INNBYGGER) }
    }

    @Test
    fun `skal kaste feil dersom person har gradering STRENGT_FORTROLIG (kode 6) `() = runWithTestContext {
        pdlClient.kode6(FNR_INNBYGGER)
        assertFailsWith<PersonNotAccessibleInPdl> { pdl.hentKommunenummer(FNR_INNBYGGER) }
    }

    @Test
    fun `skal kaste feil dersom person har gradering FORTROLIG (kode 7) `() = runWithTestContext {
        pdlClient.kode7(FNR_INNBYGGER)
        assertFailsWith<PersonNotAccessibleInPdl> { pdl.hentFornavn(FNR_INNBYGGER) }
    }
}

private fun pdlPersonResponse(json: String): PdlPersonResponse = jsonMapper.readValue(json)
