package no.nav.hjelpemidler.delbestilling.hjelpemidler

fun defaultAntall(kategori: String) = when (kategori) {
    "Dekk" -> 2
    "Hjul" -> 2
    "Svinghjul" -> 2
    "Batteri" -> 2
    else -> 1
}