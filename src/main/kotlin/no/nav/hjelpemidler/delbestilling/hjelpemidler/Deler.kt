package no.nav.hjelpemidler.delbestilling.hjelpemidler

import no.nav.hjelpemidler.delbestilling.delbestilling.Del
import no.nav.hjelpemidler.delbestilling.delbestilling.Hmsnr

private const val TODO_BESTEM_MAX_ANTALL = 8 // Finn ut hva som er et fornuftig max antall på disse

val DELER: Map<Hmsnr, Del> = listOf(
    Del(
        hmsnr = "022005",
        navn = "Batteri 80A inkl poler",
        kategori = Kategori.Batteri,
        defaultAntall = 2,
        maksAntall = 2,
    ),
    Del(
        hmsnr = "150817",
        navn = "Dekk Schwalbe Marathon Plus punkteringsbeskyttet 24\"x1",
        levArtNr = "1000038",
        kategori = Kategori.Dekk,
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/150817.jpg",
    ),
    Del(
        hmsnr = "157312",
        navn = "Hjul bak",
        kategori = Kategori.Hjul,
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/157312.png",
    ),
    Del(
        hmsnr = "157314",
        navn = "Hjul foran",
        kategori = Kategori.Hjul,
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/157314.png",
    ),
    Del(
        hmsnr = "163943",
        navn = "Hjul luft foran/bak",
        kategori = Kategori.Hjul,
        maksAntall = 4,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/163943.png",
    ),
    Del(
        hmsnr = "178498",
        navn = "Dekk Schwalbe Marathon Plus punkteringsbeskyttet 26\"x1",
        levArtNr = "1000039",
        kategori = Kategori.Dekk,
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/178498.jpg",
    ),
    Del(
        hmsnr = "184589",
        navn = "Svinghjul X smal bane 86 mm",
        levArtNr = "2000061",
        kategori = Kategori.Svinghjul,
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/184589.jpg",
    ),
    Del(
        hmsnr = "196027",
        navn = "Batteri MK 97Ah",
        levArtNr = "602293-99-0",
        kategori = Kategori.Batteri,
        defaultAntall = 2,
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/196027.png",
    ),
    Del(
        hmsnr = "196602",
        navn = "Dekk Schwalbe One 24\"x1",
        levArtNr = "1000053",
        kategori = Kategori.Dekk,
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/196602.jpg",
    ),
    Del(
        hmsnr = "200842",
        navn = "Hjul 13x5.00-6 foran/bak",
        kategori = Kategori.Hjul,
        maksAntall = 4,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/200842.png",
    ),
    Del(
        hmsnr = "202326",
        navn = "Dekk Schwalbe RightRun 26\"x1",
        levArtNr = "1000056",
        kategori = Kategori.Dekk,
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/202326.jpg",
    ),
    Del(
        hmsnr = "211449",
        navn = "Slange 24x1 bilventil",
        levArtNr = "1000005",
        kategori = Kategori.Slange,
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/211449.jpg",
    ),
    Del(
        hmsnr = "223382",
        navn = "Svinghjul kompakt 5\"/120mm",
        kategori = Kategori.Svinghjul,
        maksAntall = 2,
    ),
    Del(
        hmsnr = "232810",
        navn = "Svinghjul 90 mm",
        levArtNr = "2002010",
        kategori = Kategori.Svinghjul,
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/232810.jpg",
    ),
    Del(
        hmsnr = "234334",
        navn = "Svinghjul S3 Hjul120 mrs Panthera S3/U3 Light",
        kategori = Kategori.Svinghjul,
        maksAntall = 2,
    ),
    Del(
        hmsnr = "238403",
        navn = "Hjul bak komplett",
        levArtNr = "SP1654383",
        kategori = Kategori.Hjul,
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/238403.png",
    ),
    Del(
        hmsnr = "249612",
        navn = "Batterilader 10Ah",
        levArtNr = "SP1606544",
        kategori = Kategori.Lader,
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/249612.jpg",
    ),
    Del(
        hmsnr = "253277",
        navn = "Hjul luft sommerhjul foran/bak",
        levArtNr = "1503-1003",
        kategori = Kategori.Hjul,
        maksAntall = 4,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/253277.jpg",
    ),
    Del(
        hmsnr = "256634",
        navn = "Hjul foran luft",
        levArtNr = "SP1608593",
        kategori = Kategori.Hjul,
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/256634.png",
    ),
    Del(
        hmsnr = "256635",
        navn = "Hjul bak luft",
        levArtNr = null,
        kategori = Kategori.Hjul,
        maksAntall = 2,
    ),
    Del(
        hmsnr = "263773",
        navn = "Batteri 85 ah",
        levArtNr = "1523-1138",
        kategori = Kategori.Batteri,
        defaultAntall = 2,
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/263773.png",
    ),
    Del(
        hmsnr = "269827",
        navn = "Hjul foran komplett",
        levArtNr = "SP1654351",
        kategori = Kategori.Hjul,
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/269827.png",
    ),
    Del(
        hmsnr = "278033",
        navn = "Dekk Schwalbe RightRun 25\"x1",
        kategori = Kategori.Dekk,
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/278033.png",
    ),
    Del(
        hmsnr = "278247",
        navn = "Slange 26\"",
        kategori = Kategori.Slange,
        maksAntall = 2,
    ),
    Del(
        hmsnr = "291356",
        navn = "Hjul luft grovmønstret foran/bak",
        kategori = Kategori.Hjul,
        maksAntall = 4,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/291356.png",
    ),
    Del(
        hmsnr = "291358",
        navn = "Hjul punkteringsfri grovmønstret foran/bak",
        kategori = Kategori.Hjul,
        maksAntall = 4,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/291358.png",
    ),
    Del(
        hmsnr = "302543",
        navn = "Batterilader 10Ah",
        kategori = Kategori.Lader,
        maksAntall = 2,
        levArtNr = "1523-1103",
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/302543.jpg",
    ),
    Del(
        hmsnr = "304570",
        navn = "Batteri 12V 70-73,6 Ah C20 gele",
        levArtNr = "SPP50016",
        kategori = Kategori.Batteri,
        defaultAntall = 2,
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/304570.png",
    ),
    Del(
        hmsnr = "309144",
        navn = "Hjul foran",
        levArtNr = "1838056",
        kategori = Kategori.Hjul,
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/309144.png",
    ),
    Del(
        hmsnr = "309145",
        navn = "Hjul bak",
        levArtNr = "1838057",
        kategori = Kategori.Hjul,
        maksAntall = 2,
    ),
    Del(
        hmsnr = "309225",
        navn = "Batterilader 10Ah",
        levArtNr = "1836502",
        kategori = Kategori.Lader,
        maksAntall = 2,
    ),
    Del(
        hmsnr = "149965",
        navn = "Gripekloss seng Opus K85EW/90EW/120EW",
        levArtNr = "626550",
        kategori = Kategori.Annet,
        maksAntall = TODO_BESTEM_MAX_ANTALL,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/149965.png"
    ),
    Del(
        hmsnr = "173483",
        navn = "Håndkontroll seng Opus K85EW/90EW/120EW/SDW",
        levArtNr = "626011",
        kategori = Kategori.Annet,
        maksAntall = 1,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/173483.png"
    ),
    Del(
        hmsnr = "028152",
        navn = "Madrasstopper seng Opus K85EW/90EW/120EW",
        levArtNr = "626510",
        kategori = Kategori.Annet,
        maksAntall = TODO_BESTEM_MAX_ANTALL,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/028152.png"
    ),
    Del(
        hmsnr = "255826",
        navn = "Elektronikk seng Opus K85EW/90EW/120EW/SDW/90EW HS",
        levArtNr = "626103",
        kategori = Kategori.Annet,
        maksAntall = 1,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/255826.png"
    ),
    Del(
        hmsnr = "028158",
        navn = "Nettkabel seng Opus K85EW/90EW/120EW/SDW/90EW HS/hjertebrett",
        levArtNr = "626120",
        kategori = Kategori.Annet,
        maksAntall = 1,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/028158.png"
    ),
    Del(
        hmsnr = "149963",
        navn = "Pomrulle seng Opus K85EW/90EW/120EW/SDW/90 EW HS",
        levArtNr = "626562",
        kategori = Kategori.Annet,
        maksAntall = TODO_BESTEM_MAX_ANTALL,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/149963.png"
    ),
    Del(
        hmsnr = "232849",
        navn = "Festebrakett til sengeramme seng Opus SDW",
        levArtNr = "626796",
        kategori = Kategori.Annet,
        maksAntall = TODO_BESTEM_MAX_ANTALL,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/232849.png"
    ),
    Del(
        hmsnr = "159563",
        navn = "Batteripakke personløfter frittstå Molift Smart 150/Molift Mover 180 14,4V NiMh",
        levArtNr = "541000",
        kategori = Kategori.Batteri,
        maksAntall = 1,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/159563.png"
    ),
    Del(
        hmsnr = "262846",
        navn = "Batterilader personløfter frittstå Molift Smart 150/Molift Mover 180/Molift Mover 205 f/NiMi",
        levArtNr = "1340100",
        kategori = Kategori.Lader,
        maksAntall = 1,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/262846.png"
    ),
).also(::kontrollerForDuplikateHmsnr).associateBy { it.hmsnr }

internal fun kontrollerForDuplikateHmsnr(deler: List<Del>) {
    val duplicates = deler
        .groupBy { it }
        .filter { it.value.size > 1 }
        .flatMap { it.value }

    if (duplicates.isNotEmpty()) {
        throw IllegalStateException("DELER inneholder duplikate hmsnr: $duplicates")
    }
}