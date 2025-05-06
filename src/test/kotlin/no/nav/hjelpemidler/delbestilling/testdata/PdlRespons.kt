package no.nav.hjelpemidler.delbestilling.testdata

import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.Adressebeskyttelse
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.Bostedsadresse
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.Gradering
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.PdlError
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.PdlErrorExtension
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.PdlHentPerson
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.PdlPerson
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.PdlPersonNavn
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.PdlPersonResponse
import no.nav.hjelpemidler.delbestilling.infrastructure.pdl.Vegadresse

object PdlRespons {

    fun person(
        fornavn: String = "Fornavn",
        etternavn: String = "Etternavn",
        mellomnavn: String? = null,
        kommunenummer: String? = Testdata.defaultKommunenummer,
        adressebeskyttelse: List<Adressebeskyttelse> = emptyList(),
    ) =
        PdlPersonResponse(
            data = PdlHentPerson(
                hentPerson = PdlPerson(
                    navn = listOf(
                        PdlPersonNavn(
                            fornavn = fornavn,
                            mellomnavn = mellomnavn,
                            etternavn = etternavn
                        )
                    ),
                    bostedsadresse = listOf(
                        Bostedsadresse(
                            vegadresse = Vegadresse(
                                kommunenummer = kommunenummer,
                            )
                        )
                    ),
                    adressebeskyttelse = adressebeskyttelse
                )
            )
        )

    fun personIkkeFunnet() = PdlPersonResponse(
        data = PdlHentPerson(null),
        errors = listOf(
            PdlError(
                message = "Fant ikke person",
                extensions = PdlErrorExtension("not_found", "ExecutionAborted")
            ),
        )
    )

    fun kode6() = person(adressebeskyttelse = listOf(Adressebeskyttelse(Gradering.STRENGT_FORTROLIG)))

    fun kode7() = person(adressebeskyttelse = listOf(Adressebeskyttelse(Gradering.FORTROLIG)))

}