package no.nav.hjelpemidler.delbestilling.delbestilling

import no.nav.hjelpemidler.delbestilling.common.DelLinje
import no.nav.hjelpemidler.delbestilling.common.DellinjeUkjentDel
import no.nav.hjelpemidler.domain.geografi.Veiadresse
import no.nav.hjelpemidler.domain.person.Personnavn
import java.time.LocalDate

data class DelbestillingTilPdf(
    val mottattDato: LocalDate,
    val navnBruker: Personnavn,
    val fnrBruker: String,
    val adresseBruker: Veiadresse,
    val brukernummer: String?,
    val hjelpemiddelnavn: String,
    val hjelpemiddelHmsnr: String,
    val hjelpemiddelserienr: String? = null,
    val navnTekniker: String,
    val beskjed517: String? = null,
    val leveringsadresse: String,
    val deler: List<DelLinje>,
    val ukjenteDeler: List<DellinjeUkjentDel>,
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