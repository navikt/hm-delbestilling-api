package no.nav.hjelpemidler.delbestilling.delbestilling

import no.nav.hjelpemidler.delbestilling.isDev

private val kommunenrMedXKLager = if(isDev()) {
    setOf(
        "5616", // Hasvik
        "5405", // Vadsø
    )
} else {
    setOf(
        "0301", // Oslo
    )
}

fun harXKLager(kommunenr: String): Boolean = kommunenr in kommunenrMedXKLager