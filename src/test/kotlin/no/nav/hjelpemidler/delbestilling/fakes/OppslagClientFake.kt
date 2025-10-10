package no.nav.hjelpemidler.delbestilling.fakes

import no.nav.hjelpemidler.delbestilling.infrastructure.geografi.KommuneDto
import no.nav.hjelpemidler.delbestilling.infrastructure.geografi.OppslagClientInterface

class OppslagClientFake : OppslagClientInterface {

    val data = mapOf(
        "0301" to KommuneDto(
            fylkesnummer = "03",
            fylkesnavn = "Oslo",
            kommunenummer = "0301",
            kommunenavn = "Oslo",
        ),
    )

    override suspend fun hentKommune(kommunenr: String): KommuneDto {
        return data[kommunenr] ?: error("Mangler kommune '$kommunenr'")
    }
}