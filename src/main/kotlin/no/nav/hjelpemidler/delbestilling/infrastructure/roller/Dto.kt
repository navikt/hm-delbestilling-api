package no.nav.hjelpemidler.delbestilling.infrastructure.roller

data class Organisasjon(
    val orgnr: String,
    val navn: String,
    val orgform: String = "",
    val overordnetOrgnr: String? = null,
    val næringskoder: List<Næringskode> = emptyList(),
    val kommunenummer: String? = null,
)

data class Næringskode(
    val kode: String,
    val beskrivelse: String = "",
)

data class Delbestiller(
    val kanBestilleDeler: Boolean,

    val representasjoner: List<Organisasjon>,

    val kommunaleAnsettelsesforhold: List<Organisasjon>,
    val privateAnsettelsesforhold: List<Organisasjon>,
    // val erKommunaltAnsatt: Boolean,
    // val erAnsattIGodkjentIkkeKommunaleOrgs: Boolean,
)

data class DelbestillerResponse(
    val delbestillerrolle: Delbestiller
)