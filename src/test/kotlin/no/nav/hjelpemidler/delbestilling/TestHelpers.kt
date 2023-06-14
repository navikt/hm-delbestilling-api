package no.nav.hjelpemidler.delbestilling

import no.nav.hjelpemidler.delbestilling.delbestilling.Del
import no.nav.hjelpemidler.delbestilling.delbestilling.DelLinje
import no.nav.hjelpemidler.delbestilling.delbestilling.Delbestilling
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingRequest
import no.nav.hjelpemidler.delbestilling.delbestilling.Hmsnr
import no.nav.hjelpemidler.delbestilling.delbestilling.Levering
import no.nav.hjelpemidler.delbestilling.delbestilling.Serienr
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

fun delbestillingRequest() = DelbestillingRequest(
    Delbestilling(
        id = UUID.randomUUID(),
        hmsnr = Hmsnr("123456"),
        serienr = Serienr("123123"),
        deler = listOf(
            DelLinje(
                Del(
                    navn = "del",
                    hmsnr = "555555",
                    levArtNr = "3333333",
                    img = "",
                    kategori = "dekk",
                ),
                antall = 1,
            )
        ),
        levering = Levering.TIL_XK_LAGER,
    )
)

class MockException(msg: String) : RuntimeException("MockException: $msg")
