package no.nav.hjelpemidler.delbestilling

import no.nav.hjelpemidler.delbestilling.delbestilling.Del
import no.nav.hjelpemidler.delbestilling.delbestilling.DelLinje
import no.nav.hjelpemidler.delbestilling.delbestilling.Delbestilling
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingRequest
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingSak
import no.nav.hjelpemidler.delbestilling.delbestilling.Hmsnr
import no.nav.hjelpemidler.delbestilling.delbestilling.Levering
import no.nav.hjelpemidler.delbestilling.delbestilling.Status
import no.nav.hjelpemidler.delbestilling.oppslag.KommuneDto
import no.nav.hjelpemidler.delbestilling.roller.Delbestiller
import no.nav.hjelpemidler.delbestilling.roller.Organisasjon
import no.nav.hjelpemidler.hjelpemidlerdigitalSoknadapi.tjenester.norg.ArbeidsfordelingEnhet
import java.time.LocalDateTime
import java.util.UUID

fun delbestillerRolle(kanBestilleDeler: Boolean = true) = Delbestiller(
    kanBestilleDeler = kanBestilleDeler,
    kommunaleOrgs = listOf(Organisasjon("123", "navn", kommunenummer = "1234")),
    erKommunaltAnsatt = true,
    godkjenteIkkeKommunaleOrgs = emptyList(),
    erAnsattIGodkjentIkkeKommunaleOrgs = false,
)

fun delbestillingRequest(
    deler: List<DelLinje> = deler(),
    harOpplæringPåBatteri: Boolean = false
) = DelbestillingRequest(delbestilling(deler = deler, harOpplæringPåBatteri = harOpplæringPåBatteri))

fun delbestilling(
    deler: List<DelLinje> = deler(),
    harOpplæringPåBatteri: Boolean = false,
    hmsnr: Hmsnr = "236958",
    serienr: String = "687273",
) = Delbestilling(
    id = UUID.randomUUID(),
    hmsnr = hmsnr,
    serienr = serienr,
    deler = deler,
    levering = Levering.TIL_XK_LAGER,
    harOpplæringPåBatteri = harOpplæringPåBatteri,
    navn = "Panthera U3 Light"
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

fun delbestillingSak(
    delbestilling: Delbestilling = delbestilling(),
    opprettet: LocalDateTime = LocalDateTime.now()
) = DelbestillingSak(
    saksnummer = 37,
    delbestilling = delbestilling,
    opprettet = opprettet,
    status = Status.INNSENDT,
    sistOppdatert = LocalDateTime.now(),
    oebsOrdrenummer = "4523",
    brukersKommunenummer = "0301",
    brukersKommunenavn = "Oslo",
)

fun kommune() = KommuneDto(
    fylkesnummer = "3",
    fylkesnavn = "Oslo",
    kommunenavn = "Oslo",
    kommunenummer = "0301",
)

fun organisasjon(orgnr: String = "123456789", navn: String = "Reperasjon AS") = Organisasjon(
    orgnr = orgnr,
    navn = navn,
)

fun enhet(nummer: String = "4703") = ArbeidsfordelingEnhet(
    navn = nummer,
    enhetNr = nummer,
    type = ""
)

class MockException(msg: String) : RuntimeException("MockException: $msg")
