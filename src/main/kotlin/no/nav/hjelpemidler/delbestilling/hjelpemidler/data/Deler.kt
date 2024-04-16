package no.nav.hjelpemidler.delbestilling.hjelpemidler.data

import no.nav.hjelpemidler.delbestilling.delbestilling.Del
import no.nav.hjelpemidler.delbestilling.delbestilling.Hmsnr
import no.nav.hjelpemidler.delbestilling.hjelpemidler.Kategori
import java.time.LocalDate

private const val TODO_BESTEM_MAX_ANTALL = 8 // Finn ut hva som er et fornuftig max antall på disse

val alleDeler: Map<Hmsnr, Del> = listOf(
    Del(
        hmsnr = "022005",
        navn = "Batteri 80A inkl poler",
        kategori = Kategori.Batteri,
        defaultAntall = 2,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "150817",
        navn = "Dekk Schwalbe Marathon Plus punkteringsbeskyttet 24\"x1",
        levArtNr = "1000038",
        kategori = Kategori.Dekk,
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/150817.jpg",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "157312",
        navn = "Hjul bak",
        kategori = Kategori.Hjul,
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/157312.png",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "157314",
        navn = "Hjul foran",
        kategori = Kategori.Hjul,
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/157314.png",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "163943",
        navn = "Hjul luft foran/bak",
        kategori = Kategori.Hjul,
        maksAntall = 4,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/163943.png",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "178498",
        navn = "Dekk Schwalbe Marathon Plus punkteringsbeskyttet 26\"x1",
        levArtNr = "1000039",
        kategori = Kategori.Dekk,
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/178498.jpg",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "184589",
        navn = "Svinghjul X smal bane 86 mm",
        levArtNr = "2000061",
        kategori = Kategori.Svinghjul,
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/184589.jpg",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "196027",
        navn = "Batteri MK 97Ah",
        levArtNr = "602293-99-0",
        kategori = Kategori.Batteri,
        defaultAntall = 2,
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/196027.png",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "196602",
        navn = "Dekk Schwalbe One 24\"x1",
        levArtNr = "1000053",
        kategori = Kategori.Dekk,
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/196602.jpg",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "200842",
        navn = "Hjul 13x5.00-6 foran/bak",
        kategori = Kategori.Hjul,
        maksAntall = 4,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/200842.png",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "202326",
        navn = "Dekk Schwalbe RightRun 26\"x1",
        levArtNr = "1000056",
        kategori = Kategori.Dekk,
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/202326.jpg",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "211449",
        navn = "Slange 24x1 bilventil",
        levArtNr = "1000005",
        kategori = Kategori.Slange,
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/211449.jpg",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "223382",
        navn = "Svinghjul kompakt 5\"/120mm",
        kategori = Kategori.Svinghjul,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "232810",
        navn = "Svinghjul 90 mm",
        levArtNr = "2002010",
        kategori = Kategori.Svinghjul,
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/232810.jpg",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "234334",
        navn = "Svinghjul S3 Hjul120 mrs Panthera S3/U3 Light",
        kategori = Kategori.Svinghjul,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "238403",
        navn = "Hjul bak komplett",
        levArtNr = "SP1654383",
        kategori = Kategori.Hjul,
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/238403.png",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "249612",
        navn = "Batterilader 10Ah",
        levArtNr = "SP1606544",
        kategori = Kategori.Lader,
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/249612.jpg",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "253277",
        navn = "Hjul luft sommerhjul foran/bak",
        levArtNr = "1503-1003",
        kategori = Kategori.Hjul,
        maksAntall = 4,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/253277.jpg",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "256634",
        navn = "Hjul foran luft",
        levArtNr = "SP1608593",
        kategori = Kategori.Hjul,
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/256634.png",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "256635",
        navn = "Hjul bak luft",
        levArtNr = null,
        kategori = Kategori.Hjul,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "263773",
        navn = "Batteri 85 ah",
        levArtNr = "1523-1138",
        kategori = Kategori.Batteri,
        defaultAntall = 2,
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/263773.png",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "269827",
        navn = "Hjul foran komplett",
        levArtNr = "SP1654351",
        kategori = Kategori.Hjul,
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/269827.png",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "278033",
        navn = "Dekk Schwalbe RightRun 25\"x1",
        kategori = Kategori.Dekk,
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/278033.png",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "278247",
        navn = "Slange 26\"",
        kategori = Kategori.Slange,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "291356",
        navn = "Hjul luft grovmønstret foran/bak",
        kategori = Kategori.Hjul,
        maksAntall = 4,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/291356.png",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "291358",
        navn = "Hjul punkteringsfri grovmønstret foran/bak",
        kategori = Kategori.Hjul,
        maksAntall = 4,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/291358.png",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "302543",
        navn = "Batterilader 10Ah",
        kategori = Kategori.Lader,
        maksAntall = 2,
        levArtNr = "1523-1103",
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/302543.jpg",
        datoLagtTil = LocalDate.of(2023, 8, 18),
    ),
    Del(
        hmsnr = "304570",
        navn = "Batteri 12V 70-73,6 Ah C20 gele",
        levArtNr = "SPP50016",
        kategori = Kategori.Batteri,
        defaultAntall = 2,
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/304570.png",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "309144",
        navn = "Hjul foran",
        levArtNr = "1838056",
        kategori = Kategori.Hjul,
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/309144.png",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "309145",
        navn = "Hjul bak",
        levArtNr = "1838057",
        kategori = Kategori.Hjul,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "309225",
        navn = "Batterilader 10Ah",
        levArtNr = "1836502",
        kategori = Kategori.Lader,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2023, 8, 18),
    ),
    Del(
        hmsnr = "149965",
        navn = "Gripekloss seng Opus K85EW/90EW/120EW",
        levArtNr = "626550",
        kategori = Kategori.Annet,
        maksAntall = TODO_BESTEM_MAX_ANTALL,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/149965.png",
        datoLagtTil = LocalDate.of(2023, 11, 28),
    ),
    Del(
        hmsnr = "173483",
        navn = "Håndkontroll seng Opus K85EW/90EW/120EW/SDW",
        levArtNr = "626011",
        kategori = Kategori.Annet,
        maksAntall = 1,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/173483.png",
        datoLagtTil = LocalDate.of(2023, 11, 28),
    ),
    Del(
        hmsnr = "028152",
        navn = "Madrasstopper seng Opus K85EW/90EW/120EW",
        levArtNr = "626510",
        kategori = Kategori.Annet,
        maksAntall = TODO_BESTEM_MAX_ANTALL,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/028152.png",
        datoLagtTil = LocalDate.of(2023, 11, 28),
    ),
    Del(
        hmsnr = "255826",
        navn = "Elektronikk seng Opus K85EW/90EW/120EW/SDW/90EW HS",
        levArtNr = "626103",
        kategori = Kategori.Annet,
        maksAntall = 1,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/255826.png",
        datoLagtTil = LocalDate.of(2023, 11, 28),
    ),
    Del(
        hmsnr = "028158",
        navn = "Nettkabel seng Opus K85EW/90EW/120EW/SDW/90EW HS/hjertebrett",
        levArtNr = "626120",
        kategori = Kategori.Annet,
        maksAntall = 1,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/028158.png",
        datoLagtTil = LocalDate.of(2023, 11, 28),
    ),
    Del(
        hmsnr = "149963",
        navn = "Pomrulle seng Opus K85EW/90EW/120EW/SDW/90 EW HS",
        levArtNr = "626562",
        kategori = Kategori.Annet,
        maksAntall = TODO_BESTEM_MAX_ANTALL,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/149963.png",
        datoLagtTil = LocalDate.of(2023, 11, 28),
    ),
    Del(
        hmsnr = "232849",
        navn = "Festebrakett til sengeramme seng Opus SDW",
        levArtNr = "626796",
        kategori = Kategori.Annet,
        maksAntall = TODO_BESTEM_MAX_ANTALL,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/232849.png",
        datoLagtTil = LocalDate.of(2023, 11, 28),
    ),
    Del(
        hmsnr = "159563",
        navn = "Batteripakke personløfter frittstå Molift Smart 150/Molift Mover 180 14,4V NiMh",
        levArtNr = "541000",
        kategori = Kategori.Batteri,
        maksAntall = 1,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/159563.png",
        datoLagtTil = LocalDate.of(2023, 12, 14),
    ),
    Del(
        hmsnr = "262846",
        navn = "Batterilader personløfter frittstå Molift Smart 150/Molift Mover 180/Molift Mover 205 f/NiMi",
        levArtNr = "1340100",
        kategori = Kategori.Lader,
        maksAntall = 1,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/262846.png",
        datoLagtTil = LocalDate.of(2023, 12, 14),
    ),
    Del(
        hmsnr = "269864",
        navn = "Batteri ers Eloflex F",
        levArtNr = "7350006080111",
        kategori = Kategori.Batteri,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2024, 3, 11),
    ),
    Del(
        hmsnr = "301607",
        navn = "Batterilader ers Eloflex F 3Ah",
        levArtNr = "7350006080142",
        kategori = Kategori.Lader,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2024, 3, 11),
    ),
    Del(
        hmsnr = "301620",
        navn = "Svinghjul ers Eloflex F 8\" kompakt",
        levArtNr = "7350006080272",
        kategori = Kategori.Svinghjul,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2024, 3, 11),
    ),
    Del(
        hmsnr = "269560",
        navn = "Drivhjul ers Eloflex F 12\" luft",
        levArtNr = "7350006080258",
        kategori = Kategori.Hjul,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2024, 3, 11),
    ),
    Del(
        hmsnr = "211200",
        navn = "Batteri 73Ah ers M5 Corpus/F3 Corpus/F3 Corpus Jr/F5 Corpus/F5 Corpus VS/F5Corpus VS Jr",
        kategori = Kategori.Batteri,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2024, 3, 11),
    ),
    Del(
        hmsnr = "157311",
        navn = "Hjul 210x65 ers Permobil C500 Corpus 3G/C500 VS/K300 PS Jr/C500 VS Jr kompakt",
        kategori = Kategori.Hjul,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2024, 3, 11),
    ),
    Del(
        hmsnr = "157747",
        navn = "Hjul 3,0x8\" ers Permobil C300/C500 foran kompakt 3eiker",
        kategori = Kategori.Hjul,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2024, 3, 11),
    ),
    Del(
        hmsnr = "157310",
        navn = "Hjul 3x8\" ers Permobil C300/C500 foran luft 3eiker",
        kategori = Kategori.Hjul,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2024, 3, 11),
    ),
    Del(
        hmsnr = "168802",
        navn = "Armlene mrs Azalea kpl hø",
        levArtNr = "1517367",
        kategori = Kategori.Annet,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2024, 3, 11),
    ),
    Del(
        hmsnr = "168803",
        navn = "Armlene mrs Azalea kpl ve",
        levArtNr = "1517368",
        kategori = Kategori.Annet,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2024, 3, 11),
    ),
    Del(
        hmsnr = "198643",
        navn = "Wire mrs Azalea ryggjust l119",
        levArtNr = "1536328",
        kategori = Kategori.Annet,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2024, 3, 11),
    ),
    Del(
        hmsnr = "198644",
        navn = "Wire mrs Azalea tilt  l119",
        levArtNr = "1536325",
        kategori = Kategori.Annet,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2024, 3, 11),
    ),
    Del(
        hmsnr = "186621",
        navn = "Svinghjul mrs Azalea 8 200X 27",
        levArtNr = "5322004",
        kategori = Kategori.Svinghjul,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2024, 3, 11),
    ),
    Del(
        hmsnr = "196252",
        navn = "Håndkontroll personløfter frittstå Molift Smart 150",
        kategori = Kategori.Håndkontroll,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2024, 4, 15),
    ),
    Del(
        hmsnr = "201800",
        navn = "Håndkontroll personløfter frittstå Molift Mover 180/Molift Mover 205 4 knapper u/lading",
        kategori = Kategori.Håndkontroll,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2024, 4, 15),
    ),
    Del(
        hmsnr = "302292",
        navn = "Drivhjul ers MC1124/1144 luft std sort",
        kategori = Kategori.Hjul,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2024, 4, 15),
    ),
    Del(
        hmsnr = "253737",
        navn = "Batteri ers MC1124/MC1144 Haze 60ah gel std",
        kategori = Kategori.Batteri,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2024, 4, 15),
    ),
    Del(
        hmsnr = "263774",
        navn = "Batterilader ers MC1124/MC1144 Impulse II 8A std",
        kategori = Kategori.Lader,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2024, 4, 15),
    ),
    Del(
        hmsnr = "257597",
        navn = "Svinghjul ers MC1124 m/lager hø/ve",
        kategori = Kategori.Svinghjul,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2024, 4, 15),
    ),
    Del(
        hmsnr = "255405",
        navn = "Hjul 210x65 M5 Corpus/F3 Corpus/F5 Corpus/F5 Corpus VS/F3 Corpus Jr 2el/F3 Corpus Jr 4el/F5 Corpus VS Jr/M3 Corpus/ M3 Corpus Jr kompakt",
        kategori = Kategori.Svinghjul,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2024, 4, 15),
    ),
    Del(
        hmsnr = "255403",
        navn = "Hjul 3.00x8 M5 Corpus/F3 Corpus/F5 Corpus/F5 Corpus VS/F3 Corpus Jr 2el/F3 Corpus Jr 4el/F5 Corpus VS Jr/M3 Corpus/ M3 Corpus Jr luftfylt",
        kategori = Kategori.Hjul,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2024, 4, 15),
    ),
    Del(
        hmsnr = "264061",
        navn = "Hjul ers 180x68 M3 Corpus/M3 Corpus Jr kompakt dekk svart",
        kategori = Kategori.Svinghjul,
        defaultAntall = 4,
        maksAntall = 4,
        datoLagtTil = LocalDate.of(2024, 4, 15),
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