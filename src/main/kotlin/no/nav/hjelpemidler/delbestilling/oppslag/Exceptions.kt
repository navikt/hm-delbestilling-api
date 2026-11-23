package no.nav.hjelpemidler.delbestilling.oppslag

enum class OppslagFeil {
    TILBYR_IKKE_HJELPEMIDDEL, INGET_UTLÅN, PERSON_IKKE_FUNNET, MANGLER_BRUKERNR_ELLER_SERIENR
}

data class OppslagFeilResponse(
    val feil: OppslagFeil,
)

enum class DelOppslagFeil {
    ER_HJELPEMIDDEL
}

data class DelOppslagFeilResponse(
    val feil: DelOppslagFeil,
    val hjelpemiddelnavn: String,
)

sealed class OppslagResult {
    data class Suksess(val resultat: OppslagResultat) : OppslagResult()
    data class Feil(val feil: OppslagFeil) : OppslagResult()
}

sealed class OppslagResultUtenDeler {
    data class Suksess(val resultat: OppslagsResultatUtenDeler) : OppslagResultUtenDeler()
    data class Feil(val feil: OppslagFeil) : OppslagResultUtenDeler()
}

sealed class FinnDelerResultat {
    data class Funnet(val hjelpemiddel: Hjelpemiddel) : FinnDelerResultat()
    data class IkkeFunnet(val feil: OppslagFeil) : FinnDelerResultat()
}