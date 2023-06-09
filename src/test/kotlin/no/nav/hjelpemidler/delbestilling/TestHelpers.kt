package no.nav.hjelpemidler.delbestilling

import no.nav.hjelpemidler.delbestilling.delbestilling.DelLinje
import no.nav.hjelpemidler.delbestilling.delbestilling.Delbestilling
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingRequest
import no.nav.hjelpemidler.delbestilling.delbestilling.Hmsnr
import no.nav.hjelpemidler.delbestilling.delbestilling.Levering
import no.nav.hjelpemidler.delbestilling.delbestilling.Serienr
import no.nav.hjelpemidler.delbestilling.roller.DelbestillerResponse
import no.nav.hjelpemidler.delbestilling.roller.Organisasjon
import java.util.UUID

fun delbestillerRolle(kanBestilleDeler: Boolean = true) = DelbestillerResponse(
    kanBestilleDeler = kanBestilleDeler,
    harXKLager = true,
    kommunaleOrgs = listOf(Organisasjon("123", "navn", kommunenummer = "1234")),
    erKommunaltAnsatt = true,
    erIPilot = true,
)

fun delbestillingRequest() = DelbestillingRequest(
    Delbestilling(
        id = UUID.randomUUID(),
        hmsnr = Hmsnr("123456"),
        serienr = Serienr("123123"),
        deler = listOf(
            DelLinje(
                navn = "del",
                beskrivelse = "del mrs 24",
                hmsnr = "555555",
                levArtNr = "3333333",
                img = "",
                kategori = "dekk",
                antall = 1,
            )
        ),
        levering = Levering.TIL_XK_LAGER,
    )
)

class MockException(msg: String) : RuntimeException("MockException: $msg")