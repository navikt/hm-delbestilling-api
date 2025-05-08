package no.nav.hjelpemidler.delbestilling.oppslag

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.Attributes
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.Grunndata
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.Produkt
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.Supplier
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.data.hmsnr2Hjm
import no.nav.hjelpemidler.domain.id.UUID
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.util.UUID

class FinnDelerTilHjelpemiddelTest {

    private val grunndata = mockk<Grunndata>().apply {
        coEvery { hentProdukt("279774") } returns Produkt(
            id = UUID("027e47c8-177a-49c6-91cf-731adcc68539"),
            title = "REA Azalea",
            articleName = "REA Azalea SB44 SD43-50",
            seriesId = UUID("5cd2cce2-d954-4492-86ac-eee2d9377d10"),
            hmsArtNr = "279774",
            supplierRef = "1663952",
            attributes = Attributes(compatibleWith = null),
            isoCategory = "12220303",
            supplier = Supplier(name = "Invacare AS", id = UUID("4f296dbf-9b0e-47d3-ba74-e6e4bd5699bf")),
            media = emptyList(),
            accessory = false,
            sparePart = false,
            main = true
        )
        coEvery {
            hentDeler(
                UUID("5cd2cce2-d954-4492-86ac-eee2d9377d10"),
                UUID("027e47c8-177a-49c6-91cf-731adcc68539")
            )
        } returns emptyList()
    }

    private val finnDelerTilHjelpemiddel =
        FinnDelerTilHjelpemiddel(grunndata, mockk(relaxed = true), mockk(relaxed = true))

    @Test
    fun `skal returnere deler fra manuell liste når hjelpemiddel finnes uten deler i grunndata`() = runTest {
        val hjelpemiddel = finnDelerTilHjelpemiddel.execute("279774")

        assertNotNull(hjelpemiddel)
        assertEquals(
            hmsnr2Hjm["279774"]!!.deler.map { it.hmsnr }.toSet(),
            hjelpemiddel.deler.map { it.hmsnr }.toSet()
        )
    }
}