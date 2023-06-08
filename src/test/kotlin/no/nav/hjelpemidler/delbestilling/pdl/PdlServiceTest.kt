package no.nav.hjelpemidler.delbestilling.pdl

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class PdlServiceTest {

    private val pdlClient = mockk<PdlClient>()
    private val pdlService = PdlService(pdlClient)

    @Test
    fun `skal hente ut personnavn uten mellomnavn`() = runTest {
        coEvery { pdlClient.hentPersonNavn(any(), any()) }.returns(
            pdlPersonNavn("Arne", "Bjarne", null)
        )
        assertEquals("Arne Bjarne", pdlService.hentPersonNavn("00000000000"))
    }

    @Test
    fun `skal hente ut personnavn med mellomnavn`() = runTest {
        coEvery { pdlClient.hentPersonNavn(any(), any()) }.returns(
            pdlPersonNavn("Arne", "Bjarne", "Charlie")
        )
        assertEquals("Arne Charlie Bjarne", pdlService.hentPersonNavn("00000000000"))
    }
}

private fun pdlPersonNavn(fornavn: String, etternavn: String, mellomnavn: String? = null) =
    PdlPersonResponse(
        data = PdlHentPerson(
            hentPerson = PdlPerson(
                navn = listOf(
                    PdlPersonNavn(
                        fornavn = fornavn,
                        mellomnavn = mellomnavn,
                        etternavn = etternavn
                    )
                )
            )
        )
    )