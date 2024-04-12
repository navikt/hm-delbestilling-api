package no.nav.hjelpemidler.delbestilling.hjelpemidler

enum class Kategori {
    Annet,
    Batteri,
    Dekk,
    Hjul,
    Håndkontroll,
    Lader,
    Slange,
    Svinghjul,
}

fun defaultAntall(kategori: Kategori) = when (kategori) {
    Kategori.Dekk -> 2
    Kategori.Hjul -> 2
    Kategori.Svinghjul -> 2
    else -> 1
}