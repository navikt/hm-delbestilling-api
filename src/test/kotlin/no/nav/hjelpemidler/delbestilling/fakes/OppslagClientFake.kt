package no.nav.hjelpemidler.delbestilling.fakes

import no.nav.hjelpemidler.delbestilling.infrastructure.geografi.KommuneDto
import no.nav.hjelpemidler.delbestilling.infrastructure.geografi.OppslagClientInterface
import no.nav.hjelpemidler.delbestilling.infrastructure.geografi.PoststedDto
import no.nav.hjelpemidler.delbestilling.testdata.Kommunenummer.ASKER

class OppslagClientFake : OppslagClientInterface {

    val kommuner = mapOf(
        "0301" to KommuneDto(
            fylkesnummer = "03",
            fylkesnavn = "Oslo",
            kommunenummer = "0301",
            kommunenavn = "Oslo",
        ),
        ASKER to KommuneDto(
            fylkesnummer = "32",
            fylkesnavn = "Akershus",
            kommunenummer = ASKER,
            kommunenavn = "Asker",
        ),
    )

    val poststeder = mapOf(
        "7072" to PoststedDto(
            postnummer = "7072",
            poststed = "Heimdal",
            kommunenummer = "5001",
            kommunenavn = "Trondheim",
        )
    )

    override suspend fun hentKommune(kommunenr: String): KommuneDto {
        return kommuner[kommunenr] ?: error("Mangler kommune '$kommunenr'")
    }

    override suspend fun hentPoststed(postnummer: String): PoststedDto {
        return poststeder[postnummer] ?: error("Mangler poststed '$postnummer'")
    }
}
