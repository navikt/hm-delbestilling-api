package no.nav.hjelpemidler.delbestilling.oppslag

enum class OppslagFeil {
    TILBYR_IKKE_HJELPEMIDDEL, INGET_UTLÅN, PERSON_IKKE_FUNNET
}

data class OppslagFeilResponse(
    val feil: OppslagFeil,
)

sealed class OppslagResult {
    data class Suksess(val resultat: OppslagResultat) : OppslagResult()
    data class Feil(val feil: OppslagFeil) : OppslagResult()
}

sealed class FinnDelerResultat {
    data class Funnet(val hjelpemiddel: Hjelpemiddel) : FinnDelerResultat()
    data class IkkeFunnet(val feil: OppslagFeil) : FinnDelerResultat()
}