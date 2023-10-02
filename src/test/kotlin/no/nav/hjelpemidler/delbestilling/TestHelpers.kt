package no.nav.hjelpemidler.delbestilling

import no.nav.hjelpemidler.delbestilling.delbestilling.Del
import no.nav.hjelpemidler.delbestilling.delbestilling.DelLinje
import no.nav.hjelpemidler.delbestilling.delbestilling.Delbestilling
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingRequest
import no.nav.hjelpemidler.delbestilling.delbestilling.Levering
import no.nav.hjelpemidler.delbestilling.oppslag.KommuneDto
import no.nav.hjelpemidler.delbestilling.roller.Delbestiller
import no.nav.hjelpemidler.delbestilling.roller.Organisasjon
import java.util.UUID

fun delbestillerRolle(kanBestilleDeler: Boolean = true) = Delbestiller(
    kanBestilleDeler = kanBestilleDeler,
    harXKLager = true,
    kommunaleOrgs = listOf(Organisasjon("123", "navn", kommunenummer = "1234")),
    erKommunaltAnsatt = true,
    erIPilot = true,
)

fun delbestillingRequest(
    deler: List<DelLinje> = deler(),
    harOpplæringPåBatteri: Boolean = false
) = DelbestillingRequest(
    Delbestilling(
        id = UUID.randomUUID(),
        hmsnr = "236958",
        serienr = "687273",
        deler = deler,
        levering = Levering.TIL_XK_LAGER,
        harOpplæringPåBatteri = harOpplæringPåBatteri,
        navn = "Panthera U3 Light"
    )
)

fun deler() = listOf(
    delLinje(),
    delLinje(hmsnr = "278247", kategori = "Slange"),
)

fun delLinje(antall: Int = 1, hmsnr: String = "150817", kategori: String = "Dekk") = DelLinje(
    Del(
        navn = "del",
        hmsnr = hmsnr,
        levArtNr = "1000038",
        img = "",
        kategori = kategori,
        maksAntall = 2,
    ),
    antall = antall,
)

fun kommune() = KommuneDto(
    fylkesnummer = "3",
    fylkesnavn = "Oslo",
    kommunenavn = "Oslo",
    kommunenummer = "0301",
)

class MockException(msg: String) : RuntimeException("MockException: $msg")
