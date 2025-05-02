package no.nav.hjelpemidler.delbestilling.infrastructure.pdl

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.hjelpemidler.delbestilling.infrastructure.defaultHttpClient
import no.nav.hjelpemidler.http.openid.OpenIDClient
import no.nav.hjelpemidler.http.openid.TokenSet

fun pdlPerson(
    fornavn: String = "Fornavn",
    etternavn: String = "Etternavn",
    mellomnavn: String? = null,
    kommunenummer: String = "0301",
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
                )
            )
        )
    )

fun mockClient(response: String) = defaultHttpClient(MockEngine { respondWithBody(response) })

fun MockRequestHandleScope.respondWithBody(body: String): HttpResponseData =
    respond(
        content = ByteReadChannel(body),
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, "application/json")
    )
