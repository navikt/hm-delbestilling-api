package no.nav.hjelpemidler.delbestilling.delbestilling

import java.io.File

fun lagHjelpemidler(): List<Hjelpemiddel> {
    val linjer = File("hjelpemidler.txt").readLines()
    val hjelpemiddelTyper = listOf("Azalea", "Comet", "Cross", "Minicrosser", "Netti", "Panthera", "X850", "X850S")
    val hjelpemiddelTyperLowercase = hjelpemiddelTyper.map { it.lowercase() }

    val hjelpemidler = linjer.map { linje ->
        val (hmsnr, navn) = linje.split(" ", limit = 2)
        val navnTokens = navn.lowercase().split(" ").toSet()
        val typeIndex = hjelpemiddelTyperLowercase.indexOfFirst { navnTokens.contains(it) }

        if (typeIndex == -1) {
            println("Fant ikke gyldig type i $navn")
            return@map null
        }

        val type = hjelpemiddelTyper[typeIndex]

        Hjelpemiddel(hmsnr, navn, type)
    }.filterNotNull()

    println("hjelpemidler ${hjelpemidler.size}")

    return hjelpemidler
}

val delerMap = mapOf<String, List<Del>>(
    "Azalea" to listOf(
        Del(
            "223980",
            "Dekk 24\"",
            levArtNr = null,
            img = null,
            kategori = "Dekk"
        ),
    ),
    "Comet" to listOf(
        Del(
            "238403",
            "Hjul bak komplett",
            levArtNr = "SP1654383",
            img = null,
            kategori = "Hjul"
        ),
        Del(
            "269827",
            "Hjul foran komplett",
            levArtNr = "SP1654351",
            img = null,
            kategori = "Hjul"
        ),
        Del(
            "304570",
            "Batteri 12V 70-73,6 Ah C20 gele",
            levArtNr = "SPP50016",
            img = null,
            kategori = "Batteri"
        ),
    ),
    "Cross" to listOf(
        Del(
            "222716",
            "Svinghjul massivt hardt diam16 b3",
            levArtNr = null,
            img = null,
            kategori = "Svinghjul"
        ),
        Del(
            "223980",
            "Dekk 24\"",
            levArtNr = null,
            img = null,
            kategori = "Dekk"
        ),
    ),
    "Minicrosser" to listOf(
        Del(
            "22005",
            "Batteri 80A inkl poler",
            levArtNr = null,
            img = null,
            kategori = "Batteri"
        ),
        Del(
            "200842",
            "Hjul 13x5.00-6 ",
            levArtNr = null,
            img = null,
            kategori = "Hjul"
        ),
        Del(
            "253277",
            "Hjul 2,50-3,70-8\" luft sommerhjul",
            levArtNr = null,
            img = null,
            kategori = "Hjul"
        ),
        Del(
            "263773",
            "Hjul 2,50-3,70-8\" luft grovmønstret",
            levArtNr = "1503-1016",
            img = null,
            kategori = "Hjul"
        ),
        Del(
            "291356",
            "Batteri 85 ah",
            levArtNr = "1523-1138",
            img = null,
            kategori = "Batteri"
        ),
    )
)

data class HjelpemiddelMedDeler(
    val navn: String,
    val hmsnr: String,
    val deler: List<Del>?,
)

fun hentHjelpemiddelMedDeler(hmsnrHjelpemiddel: String): HjelpemiddelMedDeler? {
    val hjelpemiddel = lagHjelpemidler().find { it.hmsnr == hmsnrHjelpemiddel } ?: return null

    println(hjelpemiddel)

    val deler = delerMap[hjelpemiddel.type]

    return HjelpemiddelMedDeler(hjelpemiddel.navn, hjelpemiddel.hmsnr, deler)
}
