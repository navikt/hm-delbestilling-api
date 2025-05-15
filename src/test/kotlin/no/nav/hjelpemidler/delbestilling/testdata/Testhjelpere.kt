package no.nav.hjelpemidler.delbestilling.testdata

import no.nav.hjelpemidler.delbestilling.delbestilling.model.Del
import no.nav.hjelpemidler.delbestilling.delbestilling.model.DelLinje
import no.nav.hjelpemidler.delbestilling.delbestilling.model.Delbestilling
import no.nav.hjelpemidler.delbestilling.delbestilling.model.DelbestillingRequest
import no.nav.hjelpemidler.delbestilling.delbestilling.model.DelbestillingSak
import no.nav.hjelpemidler.delbestilling.delbestilling.model.Hmsnr
import no.nav.hjelpemidler.delbestilling.delbestilling.model.Lagerstatus
import no.nav.hjelpemidler.delbestilling.delbestilling.model.Levering
import no.nav.hjelpemidler.delbestilling.delbestilling.model.Status
import no.nav.hjelpemidler.delbestilling.infrastructure.roller.Delbestiller
import no.nav.hjelpemidler.delbestilling.infrastructure.roller.Organisasjon
import no.nav.hjelpemidler.hjelpemidlerdigitalSoknadapi.tjenester.norg.ArbeidsfordelingEnhet
import java.time.LocalDateTime
import java.util.UUID

fun delbestillerRolle(
    kanBestilleDeler: Boolean = true,
    kommunaleAnsettelsesforhold: List<Organisasjon> = listOf(Organisasjon("123", "navn", kommunenummer = "1234"))
) = Delbestiller(
    kanBestilleDeler = kanBestilleDeler,
    kommunaleAnsettelsesforhold = kommunaleAnsettelsesforhold,
    privateAnsettelsesforhold = emptyList(),
    representasjoner = kommunaleAnsettelsesforhold,
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

fun delLinje(antall: Int = 1, hmsnr: String = "150817", kategori: String = "Dekk", lagerstatus: Lagerstatus? = null) =
    DelLinje(
        Del(
            navn = "del",
            hmsnr = hmsnr,
            levArtNr = "1000038",
            imgs = emptyList(),
            kategori = kategori,
            maksAntall = 2,
        ),
        antall = antall,
        lagerstatusPåBestillingstidspunkt = lagerstatus,
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

fun organisasjon(orgnr: String = "123456789", navn: String = "Reperasjon AS") = Organisasjon(
    orgnr = orgnr,
    navn = navn,
)

class MockException(msg: String) : RuntimeException("MockException: $msg")
