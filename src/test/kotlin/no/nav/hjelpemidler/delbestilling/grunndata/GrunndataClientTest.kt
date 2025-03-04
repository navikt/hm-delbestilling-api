package no.nav.hjelpemidler.delbestilling.grunndata

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class GrunndataClientTest {



    @Test
    fun `test grunndataclient`() = runTest {
        val grunndataClient = GrunndataClient()
        val hjelpemiddel = grunndataClient.hentHjelpemiddel("177946")
        println(hjelpemiddel)
        val seriesId = hjelpemiddel.hits.hits.first()._source.seriesId
        val deler = grunndataClient.hentDeler(seriesId)
        println(deler)
    }


}
