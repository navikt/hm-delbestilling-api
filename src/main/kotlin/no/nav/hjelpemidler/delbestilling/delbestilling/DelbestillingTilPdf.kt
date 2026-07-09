package no.nav.hjelpemidler.delbestilling.delbestilling

import java.time.LocalDate

data class DelbestillingTilPdf(
    val mottattDato: LocalDate,
    val navnBruker: String,
    val fnrBruker: String,
    val adresseBruker: String,
    val brukernummer: String?,
    val hjelpemiddelnavn: String,
    val hjelpemiddelHmsnr: String,
    val hjelpemiddelserienr: String? = null,
    val navnTekniker: String,
    val beskjed517: String? = null,
    val leveringsadresse: String,
    val deler: List<Del>,
    val ukjenteDeler: List<UkjentDel>, // Gj
    val totalAntallDeler: Int,
)

data class Del(
    val hmsnr: String,
    val navn: String,
    val antall: Int,
)

data class UkjentDel(
    val hmsnr: String?,
    val levArtnr: String?,
    val antall: Int,
)