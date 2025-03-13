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
    fun `skal hente ut fornavn`() = runTest {
        coEvery { pdlClient.hentPersonNavn(any(), any()) }.returns(
            pdlPersonNavn(fornavn = "Arne", etternavn = "Bjarne", mellomnavn = "Charlie")
        )
        assertEquals("Arne", pdlService.hentFornavn("00000000000"))
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