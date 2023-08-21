package no.nav.hjelpemidler.delbestilling

import no.nav.hjelpemidler.delbestilling.delbestilling.Del
import no.nav.hjelpemidler.delbestilling.delbestilling.DelLinje
import no.nav.hjelpemidler.delbestilling.delbestilling.Delbestilling
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingRequest
import no.nav.hjelpemidler.delbestilling.delbestilling.Levering
import no.nav.hjelpemidler.delbestilling.delbestilling.Rolle
import no.nav.hjelpemidler.delbestilling.roller.Delbestiller
import no.nav.hjelpemidler.delbestilling.roller.Organisasjon
import java.util.UUID

fun delbestillerRolle(kanBestilleDeler: Boolean = true) = Delbestiller(
    erTekniker = true,
    erBrukerpassbruker = false,
    kanBestilleDeler = kanBestilleDeler,
    harXKLager = true,
    kommunaleOrgs = listOf(Organisasjon("123", "navn", kommunenummer = "1234")),
    erKommunaltAnsatt = true,
    erIPilot = true,
)

fun delbestillingRequest(deler: List<DelLinje> = deler()) = DelbestillingRequest(
    Delbestilling(
        id = UUID.randomUUID(),
        hmsnr = "236958",
        serienr = "687273",
        deler = deler,
        levering = Levering.TIL_XK_LAGER,
        rolle = Rolle.TEKNIKER
    )
)

fun deler() = listOf(delLinje())

fun delLinje(antall: Int = 1) = DelLinje(
    Del(
        navn = "del",
        hmsnr = "150817",
        levArtNr = "1000038",
        img = "",
        kategori = "dekk",
        maksAntall = 2,
    ),
    antall = antall,
)

class MockException(msg: String) : RuntimeException("MockException: $msg")
