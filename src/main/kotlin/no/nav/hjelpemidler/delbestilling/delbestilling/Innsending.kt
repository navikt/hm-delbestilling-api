package no.nav.hjelpemidler.delbestilling.delbestilling

import no.nav.hjelpemidler.delbestilling.common.Lager
import no.nav.hjelpemidler.delbestilling.infrastructure.roller.Organisasjon

data class Innsending(
    val bestillerFnr: String,
    val brukersFnr: String,
    val brukerKommunenr: String,
    val brukersKommunenavn: String,
    val innsendersRepresenterteOrganisasjon: Organisasjon,
    val bestillerType: BestillerType,
    val lagerEnhet: Lager,
)
