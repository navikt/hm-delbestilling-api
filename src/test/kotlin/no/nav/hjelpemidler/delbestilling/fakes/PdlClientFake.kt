package no.nav.hjelpemidler.delbestilling.fakes

import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.Adressebeskyttelse
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.Bostedsadresse
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.Gradering
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.PdlClientInterface
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.PdlError
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.PdlErrorExtension
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.PdlHentPerson
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.PdlPerson
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.PdlPersonNavn
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.PdlPersonResponse
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.Vegadresse
import no.nav.hjelpemidler.delbestilling.testdata.DEFAULT_KOMMUNENUMMER
import no.nav.hjelpemidler.delbestilling.testdata.FNR_BESTILLER
import no.nav.hjelpemidler.delbestilling.testdata.FNR_INNBYGGER

class PdlClientFake : PdlClientInterface {

    private val default = personRespons()
    private val responses = mutableMapOf(
        FNR_BESTILLER to personRespons(fornavn = "Bente", etternavn = "Bestiller"),
        FNR_INNBYGGER to personRespons(fornavn = "Inge", etternavn = "Innbygger"),
    )

    fun svar(fnr: String, response: PdlPersonResponse) {
        responses[fnr] = response
    }

    fun person(
        fnr: String,
        fornavn: String = "Fornavn",
        etternavn: String = "Etternavn",
        mellomnavn: String? = null,
        kommunenummer: String? = DEFAULT_KOMMUNENUMMER,
    ) = svar(fnr, personRespons(fornavn, etternavn, mellomnavn, kommunenummer))

    fun ikkeFunnet(fnr: String) = svar(
        fnr,
        PdlPersonResponse(
            data = PdlHentPerson(null),
            errors = listOf(
                PdlError(
                    message = "Fant ikke person",
                    extensions = PdlErrorExtension("not_found", "ExecutionAborted"),
                ),
            ),
        ),
    )

    fun kode6(fnr: String) = svar(fnr, personRespons(adressebeskyttelse = Gradering.STRENGT_FORTROLIG))

    fun kode7(fnr: String) = svar(fnr, personRespons(adressebeskyttelse = Gradering.FORTROLIG))

    private fun responsFor(fnr: String) = responses[fnr] ?: default

    override suspend fun hentKommunenummer(fnummer: String): PdlPersonResponse = responsFor(fnummer)

    override suspend fun hentPersonNavn(fnr: String): PdlPersonResponse = responsFor(fnr)

    override suspend fun hentPersonNavnOgAdresse(fnr: String): PdlPersonResponse = responsFor(fnr)
}

private fun personRespons(
    fornavn: String = "Fornavn",
    etternavn: String = "Etternavn",
    mellomnavn: String? = null,
    kommunenummer: String? = DEFAULT_KOMMUNENUMMER,
    adressebeskyttelse: Gradering? = null,
) = PdlPersonResponse(
    data = PdlHentPerson(
        hentPerson = PdlPerson(
            navn = listOf(PdlPersonNavn(fornavn = fornavn, mellomnavn = mellomnavn, etternavn = etternavn)),
            bostedsadresse = listOf(Bostedsadresse(vegadresse = Vegadresse(kommunenummer = kommunenummer))),
            adressebeskyttelse = listOfNotNull(adressebeskyttelse?.let { Adressebeskyttelse(it) }),
        )
    )
)
