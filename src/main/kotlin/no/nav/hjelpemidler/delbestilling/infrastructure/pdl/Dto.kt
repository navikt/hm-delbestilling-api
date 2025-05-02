package no.nav.hjelpemidler.delbestilling.infrastructure.pdl

import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger {}

data class PdlPersonResponse(
    val data: PdlHentPerson?,
    val errors: List<PdlError> = emptyList(),
    val extensions: PdlExtensions? = null,
)

data class PdlHentPerson(
    val hentPerson: PdlPerson?,
)

data class PdlPerson(
    val navn: List<PdlPersonNavn> = emptyList(),
    val bostedsadresse: List<Bostedsadresse> = emptyList(),
    val adressebeskyttelse: List<Adressebeskyttelse>? = emptyList(),
)

data class PdlPersonNavn(
    val fornavn: String,
    val mellomnavn: String? = null,
    val etternavn: String,
)

data class Bostedsadresse(val vegadresse: Vegadresse?)

data class Vegadresse(
    val kommunenummer: String? = null,
)

data class Adressebeskyttelse(
    val gradering: Gradering,
) {
    fun erKode6(): Boolean {
        return this.gradering == Gradering.STRENGT_FORTROLIG || this.gradering == Gradering.STRENGT_FORTROLIG_UTLAND
    }
    fun erKode7(): Boolean {
        return this.gradering == Gradering.FORTROLIG
    }
}

enum class Gradering {
    STRENGT_FORTROLIG_UTLAND,
    STRENGT_FORTROLIG,
    FORTROLIG,
    UGRADERT,
}

data class PdlError(
    val message: String,
    val locations: List<PdlErrorLocation> = emptyList(),
    val path: List<String>? = emptyList(),
    val extensions: PdlErrorExtension,
)

data class PdlErrorLocation(
    val line: Int?,
    val column: Int?,
)

data class PdlErrorExtension(
    val code: String?,
    val classification: String,
)

data class PdlExtensions(
    val warnings: List<PdlWarning> = emptyList(),
)

data class PdlWarning(
    val query: String,
    val id: String,
    val message: String,
    val details: String?,
) {
    init {
        log.error { "PDL warnings: $message. Detaljer: $details" }
    }
}