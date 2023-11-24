package no.nav.hjelpemidler.delbestilling.hjelpemidler

enum class Kategori {
    Annet,
    Batteri,
    Dekk,
    Hjul,
    Lader,
    Slange,
    Svinghjul,
}

fun defaultAntall(kategori: Kategori) = when (kategori) {
    Kategori.Batteri -> 2
    Kategori.Dekk -> 2
    Kategori.Hjul -> 2
    Kategori.Svinghjul -> 2
    Kategori.Annet -> 8
    else -> 1
}