package no.nav.hjelpemidler.delbestilling.delbestilling

import no.nav.hjelpemidler.domain.geografi.Veiadresse
import no.nav.hjelpemidler.domain.person.Personnavn

data class PersonNavnOgAdresse(
    val navn: Personnavn,
    val adresse: Veiadresse
)

