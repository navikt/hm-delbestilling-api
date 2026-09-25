package no.nav.hjelpemidler.delbestilling.oppslag.legacy.data

import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.oppslag.Del
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.DelMedHjelpemidler
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.Hjelpemiddelnavn

private const val TODO_BESTEM_MAX_ANTALL = 8 // Finn ut hva som er et fornuftig max antall på disse

val hmsnrTilDel: Map<Hmsnr, Del> = listOf(
    Del(
        hmsnr = "022005",
        navn = "Batteri 80A inkl poler",
        kategori = "Batteri",
        defaultAntall = 2,
        maksAntall = 2,
        erReservedel = true,
    ),
    Del(
        hmsnr = "150817",
        navn = "Dekk Schwalbe Marathon Plus punkteringsbeskyttet 24\"x1",
        levArtNr = "1000038",
        kategori = "Dekk",
        maksAntall = 2,
        imgs = listOf("https://storage.googleapis.com/hm_delbestilling_bilder/150817.jpg"),
        erReservedel = true,
    ),
    Del(
        hmsnr = "157312",
        navn = "Hjul bak",
        kategori = "Hjul",
        maksAntall = 2,
        imgs = listOf("https://storage.googleapis.com/hm_delbestilling_bilder/157312.png"),
        erReservedel = true,
    ),
    Del(
        hmsnr = "157314",
        navn = "Hjul foran",
        kategori = "Hjul",
        maksAntall = 2,
        imgs = listOf("https://storage.googleapis.com/hm_delbestilling_bilder/157314.png"),
        erReservedel = true,
    ),
    Del(
        hmsnr = "178498",
        navn = "Dekk Schwalbe Marathon Plus punkteringsbeskyttet 26\"x1",
        levArtNr = "1000039",
        kategori = "Dekk",
        maksAntall = 2,
        imgs = listOf("https://storage.googleapis.com/hm_delbestilling_bilder/178498.jpg"),
        erReservedel = true,
    ),
    Del(
        hmsnr = "184589",
        navn = "Svinghjul X smal bane 86 mm",
        levArtNr = "2000061",
        kategori = "Svinghjul",
        maksAntall = 2,
        imgs = listOf("https://storage.googleapis.com/hm_delbestilling_bilder/184589.jpg"),
        erReservedel = true,
    ),
    Del(
        hmsnr = "196027",
        navn = "Batteri MK 97Ah",
        levArtNr = "602293-99-0",
        kategori = "Batteri",
        defaultAntall = 2,
        maksAntall = 2,
        imgs = listOf("https://storage.googleapis.com/hm_delbestilling_bilder/196027.png"),
        erReservedel = true,
    ),
    Del(
        hmsnr = "196602",
        navn = "Dekk Schwalbe One 24\"x1",
        levArtNr = "1000053",
        kategori = "Dekk",
        maksAntall = 2,
        imgs = listOf("https://storage.googleapis.com/hm_delbestilling_bilder/196602.jpg"),
        erReservedel = true,
    ),
    Del(
        hmsnr = "200842",
        navn = "Hjul 13x5.00-6 foran/bak",
        kategori = "Hjul",
        maksAntall = 4,
        imgs = listOf("https://storage.googleapis.com/hm_delbestilling_bilder/200842.png"),
        erReservedel = true,
    ),
    Del(
        hmsnr = "202326",
        navn = "Dekk Schwalbe RightRun 26\"x1",
        levArtNr = "1000056",
        kategori = "Dekk",
        maksAntall = 2,
        imgs = listOf("https://storage.googleapis.com/hm_delbestilling_bilder/202326.jpg"),
        erReservedel = true,
    ),
    Del(
        hmsnr = "211449",
        navn = "Slange 24x1 bilventil",
        levArtNr = "1000005",
        kategori = "Slange",
        maksAntall = 2,
        imgs = listOf("https://storage.googleapis.com/hm_delbestilling_bilder/211449.jpg"),
        erReservedel = true,
    ),
    Del(
        hmsnr = "223382",
        navn = "Svinghjul 150 Panthera S3/U3/S3 Swing/S3 Junior/S3 Junior Allround",
        kategori = "Svinghjul",
        maksAntall = 2,
        erReservedel = true,
    ),
    Del(
        hmsnr = "232810",
        navn = "Svinghjul 90 mm",
        levArtNr = "2002010",
        kategori = "Svinghjul",
        maksAntall = 2,
        imgs = listOf("https://storage.googleapis.com/hm_delbestilling_bilder/232810.jpg"),
        erReservedel = true,
    ),
    Del(
        hmsnr = "234334",
        navn = "Svinghjul S3 Hjul120 mrs Panthera S3/U3 Light",
        kategori = "Svinghjul",
        maksAntall = 2,
        erReservedel = true,
    ),
    Del(
        hmsnr = "253277",
        navn = "Hjul luft sommerhjul foran/bak",
        levArtNr = "1503-1003",
        kategori = "Hjul",
        maksAntall = 4,
        imgs = listOf("https://storage.googleapis.com/hm_delbestilling_bilder/253277.jpg"),
        erReservedel = true,
    ),
    Del(
        hmsnr = "328154", // Tidligere 263773
        navn = "Batteri 85 Ah",
        levArtNr = "1523-1157",
        kategori = "Batteri",
        defaultAntall = 2,
        maksAntall = 2,
        erReservedel = true,
    ),
    Del(
        hmsnr = "278033",
        navn = "Dekk Schwalbe RightRun 25\"x1",
        kategori = "Dekk",
        maksAntall = 2,
        imgs = listOf("https://storage.googleapis.com/hm_delbestilling_bilder/278033.png"),
        erReservedel = true,
    ),
    Del(
        hmsnr = "278247",
        navn = "Slange 26\"",
        kategori = "Slange",
        maksAntall = 2,
        erReservedel = true,
    ),
    Del(
        hmsnr = "291356",
        navn = "Hjul luft grovmønstret foran/bak",
        kategori = "Hjul",
        maksAntall = 4,
        imgs = listOf("https://storage.googleapis.com/hm_delbestilling_bilder/291356.png"),
        erReservedel = true,
    ),
    Del(
        hmsnr = "291358",
        navn = "Hjul punkteringsfri grovmønstret foran/bak",
        kategori = "Hjul",
        maksAntall = 4,
        imgs = listOf("https://storage.googleapis.com/hm_delbestilling_bilder/291358.png"),
        erReservedel = true,
    ),
    Del(
        hmsnr = "302543",
        navn = "Batterilader 10Ah",
        kategori = "Lader",
        maksAntall = 2,
        levArtNr = "1523-1103",
        imgs = listOf("https://storage.googleapis.com/hm_delbestilling_bilder/302543.jpg"),
        erReservedel = true,
    ),
    Del(
        hmsnr = "309225",
        navn = "Batterilader 10Ah",
        levArtNr = "1836502",
        kategori = "Lader",
        maksAntall = 2,
        erReservedel = true,
    ),
    Del(
        hmsnr = "269864",
        navn = "Batteri ers Eloflex F",
        levArtNr = "7350006080111",
        kategori = "Batteri",
        maksAntall = 2,
        erReservedel = true,
    ),
    Del(
        hmsnr = "301607",
        navn = "Batterilader ers Eloflex F 3Ah",
        levArtNr = "7350006080142",
        kategori = "Lader",
        maksAntall = 1,
        erReservedel = true,
    ),
    Del(
        hmsnr = "301620",
        navn = "Svinghjul ers Eloflex F 8\" kompakt",
        levArtNr = "7350006080272",
        kategori = "Svinghjul",
        maksAntall = 2,
        erReservedel = true,
    ),
    Del(
        hmsnr = "269560",
        navn = "Drivhjul ers Eloflex F 12\" luft",
        levArtNr = "7350006080258",
        kategori = "Hjul",
        maksAntall = 2,
        erReservedel = true,
    ),
    Del(
        hmsnr = "211200",
        navn = "Batteri 73Ah ers M5 Corpus/F3 Corpus/F3 Corpus Jr/F5 Corpus/F5 Corpus VS/F5Corpus VS Jr",
        kategori = "Batteri",
        maksAntall = 2,
        erReservedel = true,
    ),
    Del(
        hmsnr = "157311",
        navn = "Hjul 210x65 ers Permobil C500 Corpus 3G/C500 VS/K300 PS Jr/C500 VS Jr kompakt",
        kategori = "Hjul",
        maksAntall = 2,
        erReservedel = true,
    ),
    Del(
        hmsnr = "157747",
        navn = "Hjul 3,0x8\" ers Permobil C300/C500 foran kompakt 3eiker",
        kategori = "Hjul",
        maksAntall = 2,
        erReservedel = true,
    ),
    Del(
        hmsnr = "157310",
        navn = "Hjul 3x8\" ers Permobil C300/C500 foran luft 3eiker",
        kategori = "Hjul",
        maksAntall = 2,
        erReservedel = true,
    ),
    Del(
        hmsnr = "168802",
        navn = "Armlene mrs Azalea kpl hø",
        levArtNr = "1517367",
        kategori = "Annet",
        maksAntall = 1,
        erReservedel = true,
    ),
    Del(
        hmsnr = "168803",
        navn = "Armlene mrs Azalea kpl ve",
        levArtNr = "1517368",
        kategori = "Annet",
        maksAntall = 1,
        erReservedel = true,
    ),
    Del(
        hmsnr = "198643",
        navn = "Wire mrs Azalea ryggjust l119",
        levArtNr = "1536328",
        kategori = "Annet",
        maksAntall = 1,
        erReservedel = true,
    ),
    Del(
        hmsnr = "198644",
        navn = "Wire mrs Azalea tilt  l119",
        levArtNr = "1536325",
        kategori = "Annet",
        maksAntall = 1,
        erReservedel = true,
    ),
    Del(
        hmsnr = "186621",
        navn = "Svinghjul mrs Azalea 8 200X 27",
        levArtNr = "5322004",
        kategori = "Svinghjul",
        maksAntall = 2,
        erReservedel = true,
    ),
    Del(
        hmsnr = "255405",
        navn = "Hjul 210x65 M5 Corpus/F3 Corpus/F5 Corpus/F5 Corpus VS/F3 Corpus Jr 2el/F3 Corpus Jr 4el/F5 Corpus VS Jr/M3 Corpus/ M3 Corpus Jr kompakt",
        kategori = "Svinghjul",
        maksAntall = 2,
        erReservedel = true,
    ),
    Del(
        hmsnr = "255403",
        navn = "Hjul 3.00x8 M5 Corpus/F3 Corpus/F5 Corpus/F5 Corpus VS/F3 Corpus Jr 2el/F3 Corpus Jr 4el/F5 Corpus VS Jr/M3 Corpus/ M3 Corpus Jr luftfylt",
        kategori = "Hjul",
        maksAntall = 2,
        erReservedel = true,
    ),
).associateBy { it.hmsnr }


val hmsnrTilDelMedHjelpemiddel: Map<Hmsnr, DelMedHjelpemidler> = hmsnrTilDel.mapValues { (hmsnrDel, del) ->
    // Finn hvilke hjelpemiddel som har en kobling til denne delen
    val hjmHmsnrForDel = mutableSetOf<Hmsnr>()
    hmsnrHjmTilHmsnrDeler.forEach { (hmsnrHjm, delerTilHjm) ->
        if (hmsnrDel in delerTilHjm) {
            hjmHmsnrForDel.add(hmsnrHjm)
        }
    }

    // Map hjm hmsnr til Hjelpemiddel
    val hjelpemidler = hjmHmsnrForDel.map {
        Hjelpemiddelnavn(
            navn = hmsnrTilHjelpemiddelnavn[it]?.navn
                ?: throw IllegalArgumentException("Mangler navn for hjelpemiddel $it"),
            hmsnr = it,
            isoKode = hmsnrTilHjelpemiddelnavn[it]?.isoKode ?: error("Fant ikke isoKode for hjm $it"),
        )
    }

    DelMedHjelpemidler(
        del = del,
        hjelpemidler = hjelpemidler
    )
}