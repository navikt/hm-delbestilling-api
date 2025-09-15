package no.nav.hjelpemidler.delbestilling.oppslag

import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Utlån
import java.time.LocalDate

class BerikMedGaranti() {
    operator fun invoke(hjelpemiddel: Hjelpemiddel, utlån: Utlån, nå: LocalDate): Hjelpemiddel {
        val beriket = hjelpemiddel.medGaranti(utlån, nå)
        return beriket
    }
}