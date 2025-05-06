package no.nav.hjelpemidler.delbestilling.oppslag

import io.ktor.http.HttpStatusCode

enum class OppslagFeil {
    TILBYR_IKKE_HJELPEMIDDEL, INGET_UTLÅN, IKKE_HOVEDHJELPEMIDDEL
}

data class OppslagFeilResponse(
    val feil: OppslagFeil,
)

sealed class OppslagException(
    message: String,
    val status: HttpStatusCode,
    val feil: OppslagFeil,
) : RuntimeException(message)

class IkkeHjelpemiddelException(message: String) :
    OppslagException(message, HttpStatusCode.NotFound, OppslagFeil.IKKE_HOVEDHJELPEMIDDEL)

class IngenUtlånException(message: String) :
    OppslagException(message, HttpStatusCode.NotFound, OppslagFeil.INGET_UTLÅN)

class TilbyrIkkeHjelpemiddelException(message: String) :
        OppslagException(message, HttpStatusCode.NotFound, OppslagFeil.TILBYR_IKKE_HJELPEMIDDEL)