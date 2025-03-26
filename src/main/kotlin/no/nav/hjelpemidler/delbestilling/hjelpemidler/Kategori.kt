package no.nav.hjelpemidler.delbestilling.hjelpemidler

fun defaultAntall(kategori: String) = when (kategori) {
    "Dekk" -> 2
    "Hjul" -> 2
    "Svinghjul" -> 2
    "Batteri" -> 2
    "Drivhjul" -> 2
    else -> 1
}

private val ISOKODE_PERSONLØFTER = "1236"
private val ISOKODE_ERS = "1223"

fun maksAntall(kategori: String, isoKode: String): Int = when (kategori) {
    "Batteri" -> maksAntallBatteri(isoKode.take(4))
    else -> 4
}

fun maksAntallBatteri(isoKode: String): Int = when(isoKode) {
    ISOKODE_PERSONLØFTER -> 1
    ISOKODE_ERS -> 2
    else -> 4
}