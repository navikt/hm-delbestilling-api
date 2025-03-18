package no.nav.hjelpemidler.delbestilling.hjelpemidler.data

import no.nav.hjelpemidler.delbestilling.delbestilling.Del
import no.nav.hjelpemidler.delbestilling.delbestilling.Hmsnr
import no.nav.hjelpemidler.delbestilling.hjelpemidler.DelMedHjelpemidler
import no.nav.hjelpemidler.delbestilling.hjelpemidler.Hjelpemiddel
import java.time.LocalDate
import kotlin.also

private const val TODO_BESTEM_MAX_ANTALL = 8 // Finn ut hva som er et fornuftig max antall på disse

val hmsnrTilDel: Map<Hmsnr, Del> = listOf<Del>(
    Del(
        hmsnr = "022005",
        navn = "Batteri 80A inkl poler",
        kategori = "Batteri",
        defaultAntall = 2,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "150817",
        navn = "Dekk Schwalbe Marathon Plus punkteringsbeskyttet 24\"x1",
        levArtNr = "1000038",
        kategori = "Dekk",
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/150817.jpg",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "157312",
        navn = "Hjul bak",
        kategori = "Hjul",
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/157312.png",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "157314",
        navn = "Hjul foran",
        kategori = "Hjul",
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/157314.png",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "178498",
        navn = "Dekk Schwalbe Marathon Plus punkteringsbeskyttet 26\"x1",
        levArtNr = "1000039",
        kategori = "Dekk",
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/178498.jpg",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "184589",
        navn = "Svinghjul X smal bane 86 mm",
        levArtNr = "2000061",
        kategori = "Svinghjul",
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/184589.jpg",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "196027",
        navn = "Batteri MK 97Ah",
        levArtNr = "602293-99-0",
        kategori = "Batteri",
        defaultAntall = 2,
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/196027.png",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "196602",
        navn = "Dekk Schwalbe One 24\"x1",
        levArtNr = "1000053",
        kategori = "Dekk",
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/196602.jpg",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "200842",
        navn = "Hjul 13x5.00-6 foran/bak",
        kategori = "Hjul",
        maksAntall = 4,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/200842.png",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "202326",
        navn = "Dekk Schwalbe RightRun 26\"x1",
        levArtNr = "1000056",
        kategori = "Dekk",
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/202326.jpg",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "211449",
        navn = "Slange 24x1 bilventil",
        levArtNr = "1000005",
        kategori = "Slange",
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/211449.jpg",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "223382",
        navn = "Svinghjul 150 Panthera S3/U3/S3 Swing/S3 Junior/S3 Junior Allround",
        kategori = "Svinghjul",
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "232810",
        navn = "Svinghjul 90 mm",
        levArtNr = "2002010",
        kategori = "Svinghjul",
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/232810.jpg",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "234334",
        navn = "Svinghjul S3 Hjul120 mrs Panthera S3/U3 Light",
        kategori = "Svinghjul",
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "238403",
        navn = "Hjul bak komplett",
        levArtNr = "SP1654383",
        kategori = "Hjul",
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/238403.png",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "249612",
        navn = "Batterilader 10Ah",
        levArtNr = "SP1606544",
        kategori = "Lader",
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/249612.jpg",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "253277",
        navn = "Hjul luft sommerhjul foran/bak",
        levArtNr = "1503-1003",
        kategori = "Hjul",
        maksAntall = 4,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/253277.jpg",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "256634",
        navn = "Hjul foran luft",
        levArtNr = "SP1608593",
        kategori = "Hjul",
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/256634.png",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "256635",
        navn = "Hjul bak luft",
        levArtNr = null,
        kategori = "Hjul",
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "328154", // Tidligere 263773
        navn = "Batteri 85 Ah",
        levArtNr = "1523-1157",
        kategori = "Batteri",
        defaultAntall = 2,
        maksAntall = 2,
        // TODO kan vi gjenbruke dette gamle bildet? img = "https://storage.googleapis.com/hm_delbestilling_bilder/263773.png",
        datoLagtTil = LocalDate.of(2025, 1, 22),
    ),
    Del(
        hmsnr = "269827",
        navn = "Hjul foran komplett",
        levArtNr = "SP1654351",
        kategori = "Hjul",
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/269827.png",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "278033",
        navn = "Dekk Schwalbe RightRun 25\"x1",
        kategori = "Dekk",
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/278033.png",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "278247",
        navn = "Slange 26\"",
        kategori = "Slange",
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "291356",
        navn = "Hjul luft grovmønstret foran/bak",
        kategori = "Hjul",
        maksAntall = 4,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/291356.png",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "291358",
        navn = "Hjul punkteringsfri grovmønstret foran/bak",
        kategori = "Hjul",
        maksAntall = 4,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/291358.png",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "302543",
        navn = "Batterilader 10Ah",
        kategori = "Lader",
        maksAntall = 2,
        levArtNr = "1523-1103",
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/302543.jpg",
        datoLagtTil = LocalDate.of(2023, 8, 18),
    ),
    Del(
        hmsnr = "304570",
        navn = "Batteri 12V 70-73,6 Ah C20 gele",
        levArtNr = "SPP50016",
        kategori = "Batteri",
        defaultAntall = 2,
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/304570.png",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "309144",
        navn = "Hjul foran",
        levArtNr = "1838056",
        kategori = "Hjul",
        maksAntall = 2,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/309144.png",
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "309145",
        navn = "Hjul bak",
        levArtNr = "1838057",
        kategori = "Hjul",
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2023, 6, 20),
    ),
    Del(
        hmsnr = "309225",
        navn = "Batterilader 10Ah",
        levArtNr = "1836502",
        kategori = "Lader",
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2023, 8, 18),
    ),
    Del(
        hmsnr = "149965",
        navn = "Gripekloss seng Opus K85EW/90EW/120EW",
        levArtNr = "626550",
        kategori = "Annet",
        maksAntall = TODO_BESTEM_MAX_ANTALL,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/149965.png",
        datoLagtTil = LocalDate.of(2023, 11, 28),
    ),
    Del(
        hmsnr = "173483",
        navn = "Håndkontroll seng Opus K85EW/90EW/120EW/SDW",
        levArtNr = "626011",
        kategori = "Annet",
        maksAntall = 1,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/173483.png",
        datoLagtTil = LocalDate.of(2023, 11, 28),
    ),
    Del(
        hmsnr = "028152",
        navn = "Madrasstopper seng Opus K85EW/90EW/120EW",
        levArtNr = "626510",
        kategori = "Annet",
        maksAntall = TODO_BESTEM_MAX_ANTALL,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/028152.png",
        datoLagtTil = LocalDate.of(2023, 11, 28),
    ),
    Del(
        hmsnr = "255826",
        navn = "Elektronikk seng Opus K85EW/90EW/120EW/SDW/90EW HS",
        levArtNr = "626103",
        kategori = "Annet",
        maksAntall = 1,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/255826.png",
        datoLagtTil = LocalDate.of(2023, 11, 28),
    ),
    Del(
        hmsnr = "028158",
        navn = "Nettkabel seng Opus K85EW/90EW/120EW/SDW/90EW HS/hjertebrett",
        levArtNr = "626120",
        kategori = "Annet",
        maksAntall = 1,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/028158.png",
        datoLagtTil = LocalDate.of(2023, 11, 28),
    ),
    Del(
        hmsnr = "149963",
        navn = "Pomrulle seng Opus K85EW/90EW/120EW/SDW/90 EW HS",
        levArtNr = "626562",
        kategori = "Annet",
        maksAntall = TODO_BESTEM_MAX_ANTALL,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/149963.png",
        datoLagtTil = LocalDate.of(2023, 11, 28),
    ),
    Del(
        hmsnr = "232849",
        navn = "Festebrakett til sengeramme seng Opus SDW",
        levArtNr = "626796",
        kategori = "Annet",
        maksAntall = TODO_BESTEM_MAX_ANTALL,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/232849.png",
        datoLagtTil = LocalDate.of(2023, 11, 28),
    ),
    Del(
        hmsnr = "159563",
        navn = "Batteripakke personløfter frittstå Molift Smart 150/Molift Mover 180 14,4V NiMh",
        levArtNr = "541000",
        kategori = "Batteripakke",
        maksAntall = 1,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/159563.png",
        datoLagtTil = LocalDate.of(2023, 12, 14),
    ),
    Del(
        hmsnr = "262846",
        navn = "Batterilader personløfter frittstå Molift Smart 150/Molift Mover 180/Molift Mover 205 f/NiMi",
        levArtNr = "1340100",
        kategori = "Lader",
        maksAntall = 1,
        img = "https://storage.googleapis.com/hm_delbestilling_bilder/262846.png",
        datoLagtTil = LocalDate.of(2023, 12, 14),
    ),
    Del(
        hmsnr = "269864",
        navn = "Batteri ers Eloflex F",
        levArtNr = "7350006080111",
        kategori = "Batteri",
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2024, 3, 11),
    ),
    Del(
        hmsnr = "301607",
        navn = "Batterilader ers Eloflex F 3Ah",
        levArtNr = "7350006080142",
        kategori = "Lader",
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2024, 3, 11),
    ),
    Del(
        hmsnr = "301620",
        navn = "Svinghjul ers Eloflex F 8\" kompakt",
        levArtNr = "7350006080272",
        kategori = "Svinghjul",
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2024, 3, 11),
    ),
    Del(
        hmsnr = "269560",
        navn = "Drivhjul ers Eloflex F 12\" luft",
        levArtNr = "7350006080258",
        kategori = "Hjul",
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2024, 3, 11),
    ),
    Del(
        hmsnr = "211200",
        navn = "Batteri 73Ah ers M5 Corpus/F3 Corpus/F3 Corpus Jr/F5 Corpus/F5 Corpus VS/F5Corpus VS Jr",
        kategori = "Batteri",
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2024, 3, 11),
    ),
    Del(
        hmsnr = "157311",
        navn = "Hjul 210x65 ers Permobil C500 Corpus 3G/C500 VS/K300 PS Jr/C500 VS Jr kompakt",
        kategori = "Hjul",
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2024, 3, 11),
    ),
    Del(
        hmsnr = "157747",
        navn = "Hjul 3,0x8\" ers Permobil C300/C500 foran kompakt 3eiker",
        kategori = "Hjul",
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2024, 3, 11),
    ),
    Del(
        hmsnr = "157310",
        navn = "Hjul 3x8\" ers Permobil C300/C500 foran luft 3eiker",
        kategori = "Hjul",
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2024, 3, 11),
    ),
    Del(
        hmsnr = "168802",
        navn = "Armlene mrs Azalea kpl hø",
        levArtNr = "1517367",
        kategori = "Annet",
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2024, 3, 11),
    ),
    Del(
        hmsnr = "168803",
        navn = "Armlene mrs Azalea kpl ve",
        levArtNr = "1517368",
        kategori = "Annet",
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2024, 3, 11),
    ),
    Del(
        hmsnr = "198643",
        navn = "Wire mrs Azalea ryggjust l119",
        levArtNr = "1536328",
        kategori = "Annet",
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2024, 3, 11),
    ),
    Del(
        hmsnr = "198644",
        navn = "Wire mrs Azalea tilt  l119",
        levArtNr = "1536325",
        kategori = "Annet",
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2024, 3, 11),
    ),
    Del(
        hmsnr = "186621",
        navn = "Svinghjul mrs Azalea 8 200X 27",
        levArtNr = "5322004",
        kategori = "Svinghjul",
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2024, 3, 11),
    ),
    Del(
        hmsnr = "196252",
        navn = "Håndkontroll personløfter frittstå Molift Smart 150",
        kategori = "Håndkontroll",
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2024, 4, 15),
    ),
    Del(
        hmsnr = "201800",
        navn = "Håndkontroll personløfter frittstå Molift Mover 180/Molift Mover 205 4 knapper u/lading",
        kategori = "Håndkontroll",
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2024, 4, 15),
    ),
    Del(
        hmsnr = "302292",
        navn = "Drivhjul ers MC1124/1144 luft std sort",
        kategori = "Hjul",
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2024, 4, 15),
    ),
    Del(
        hmsnr = "253737",
        navn = "Batteri ers MC1124/MC1144 Haze 60ah gel std",
        kategori = "Batteri",
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2024, 4, 15),
    ),
    Del(
        hmsnr = "263774",
        navn = "Batterilader ers MC1124/MC1144 Impulse II 8A std",
        kategori = "Lader",
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2024, 4, 15),
    ),
    Del(
        hmsnr = "257597",
        navn = "Svinghjul ers MC1124 m/lager hø/ve",
        kategori = "Svinghjul",
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2024, 4, 15),
    ),
    Del(
        hmsnr = "255405",
        navn = "Hjul 210x65 M5 Corpus/F3 Corpus/F5 Corpus/F5 Corpus VS/F3 Corpus Jr 2el/F3 Corpus Jr 4el/F5 Corpus VS Jr/M3 Corpus/ M3 Corpus Jr kompakt",
        kategori = "Svinghjul",
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2024, 4, 15),
    ),
    Del(
        hmsnr = "255403",
        navn = "Hjul 3.00x8 M5 Corpus/F3 Corpus/F5 Corpus/F5 Corpus VS/F3 Corpus Jr 2el/F3 Corpus Jr 4el/F5 Corpus VS Jr/M3 Corpus/ M3 Corpus Jr luftfylt",
        kategori = "Hjul",
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2024, 4, 15),
    ),
    Del(
        hmsnr = "140928",
        navn = "Dekk helgummi",
        levArtNr = "80768",
        kategori = "Dekk",
        defaultAntall = 2,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "004392",
        navn = "Armlene lang høyre",
        levArtNr = "25173-1",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "004393",
        navn = "Armlene lang venstre",
        levArtNr = "25173-2",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "025943",
        navn = "Kneledd",
        levArtNr = "25574-01",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "016127",
        navn = "Fotplate høyre",
        levArtNr = "25641-1",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "016129",
        navn = "Fotplate høyre",
        levArtNr = "25642-1",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "016131",
        navn = "Fotplate høyre",
        levArtNr = "25643-1",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "016133",
        navn = "Fotplate høyre",
        levArtNr = "25644-1",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "016135",
        navn = "Fotplate høyre",
        levArtNr = "25645-1",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "016137",
        navn = "Fotplate høyre",
        levArtNr = "25736-1",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "016139",
        navn = "Fotplate høyre",
        levArtNr = "25646-1",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "016126",
        navn = "Fotplate venstre",
        levArtNr = "25641-2",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "016128",
        navn = "Fotplate venstre",
        levArtNr = "25641-2",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "016130",
        navn = "Fotplate venstre",
        levArtNr = "25643-2",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "016132",
        navn = "Fotplate venstre",
        levArtNr = "25644-2",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "016134",
        navn = "Fotplate venstre",
        levArtNr = "25645-2",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "016136",
        navn = "Fotplate venstre",
        levArtNr = "25736-2",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "016138",
        navn = "Fotplate venstre",
        levArtNr = "25646-2",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "220485",
        navn = "Drivhjul",
        levArtNr = "27564",
        kategori = "Hjul",
        defaultAntall = 2,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "318078",
        navn = "Drivhjul",
        levArtNr = "83879",
        kategori = "Hjul",
        defaultAntall = 2,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "181134",
        navn = "Drivhjul",
        levArtNr = "81346",
        kategori = "Hjul",
        defaultAntall = 2,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "318227",
        navn = "Hurtigkobling 12,7 mm",
        levArtNr = "83872",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "219993",
        navn = "Hurtigkobling 12 mm",
        levArtNr = "82483",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "139108",
        navn = "Hurtigkobling 12 mm",
        levArtNr = "81325",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "154111",
        navn = "Gaffel svinghjul",
        levArtNr = "24678",
        kategori = "Svinghjul",
        defaultAntall = 2,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "318335",
        navn = "Gaffel svinghjul",
        levArtNr = "28969",
        kategori = "Svinghjul",
        defaultAntall = 2,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "222716",
        navn = "Svinghjul standard",
        levArtNr = "27866",
        kategori = "Svinghjul",
        defaultAntall = 2,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "318324",
        navn = "Svinghjul standard",
        levArtNr = "28922",
        kategori = "Svinghjul",
        defaultAntall = 2,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "220326",
        navn = "Brems høyre",
        levArtNr = "27528-1",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "220325",
        navn = "Brems venste",
        levArtNr = "27528-2",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "158029",
        navn = "Setetrekk kort",
        levArtNr = "62718-60",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "158033",
        navn = "Setetrekk lang",
        levArtNr = "62728-60",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "240197",
        navn = "Setetrekk kort",
        levArtNr = "62719-60",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "279255",
        navn = "Setetrekk lang",
        levArtNr = "62729-60",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "240198",
        navn = "Setetrekk kort",
        levArtNr = "62720-60",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "236921",
        navn = "Setetrekk lang",
        levArtNr = "62730-60",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "240199",
        navn = "Setetrekk kort",
        levArtNr = "62721-60",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "236922",
        navn = "Setetrekk lang",
        levArtNr = "62731-60",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "240200",
        navn = "Setetrekk kort",
        levArtNr = "62722-60",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "236923",
        navn = "Setetrekk lang",
        levArtNr = "62732-60",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "240201",
        navn = "Setetrekk kort",
        levArtNr = "62978-60",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "236924",
        navn = "Setetrekk lang",
        levArtNr = "62984-60",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "240202",
        navn = "Setetrekk kort",
        levArtNr = "62979-60",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "236925",
        navn = "Setetrekk lang",
        levArtNr = "62985-60",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "240203",
        navn = "Setetrekk kort",
        levArtNr = "62980-60",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "279256",
        navn = "Setetrekk lang",
        levArtNr = "62986-60",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "240204",
        navn = "Setetrekk kort",
        levArtNr = "62981-60",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "279257",
        navn = "Setetrekk lang",
        levArtNr = "62987-60",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "240205",
        navn = "Setetrekk kort",
        levArtNr = "62982-60",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "279258",
        navn = "Setetrekk lang",
        levArtNr = "62988-60",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "240206",
        navn = "Setetrekk kort",
        levArtNr = "62983-60",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "181078",
        navn = "Overdrag",
        levArtNr = "62941-60",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "181079",
        navn = "Overdrag",
        levArtNr = "62942-60",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "181080",
        navn = "Overdrag",
        levArtNr = "62943-60",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "181081",
        navn = "Overdrag",
        levArtNr = "62944-60",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "181082",
        navn = "Overdrag",
        levArtNr = "62945-60",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "181083",
        navn = "Overdrag",
        levArtNr = "62946-60",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "181084",
        navn = "Overdrag",
        levArtNr = "62947-60",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "181085",
        navn = "Overdrag",
        levArtNr = "62948-60",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "181086",
        navn = "Overdrag",
        levArtNr = "62949-60",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "181087",
        navn = "Overdrag",
        levArtNr = "62950-60",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "181088",
        navn = "Overdrag",
        levArtNr = "62951-60",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "181089",
        navn = "Ryggtrekk",
        levArtNr = "62953-60",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "181090",
        navn = "Ryggtrekk",
        levArtNr = "62954-60",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "181091",
        navn = "Ryggtrekk",
        levArtNr = "62955-60",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "006428",
        navn = "Armlene høyre",
        levArtNr = "21682",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "316171",
        navn = "Armlene høyre",
        levArtNr = "93727",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "006429",
        navn = "Armlene venstre",
        levArtNr = "93728",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "316172",
        navn = "Armlene venstre",
        levArtNr = "93728",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "065633",
        navn = "Armlene pad",
        levArtNr = "20835",
        kategori = "Annet",
        defaultAntall = 2,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "154652",
        navn = "Benstøtte høyre",
        levArtNr = "29500",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "154653",
        navn = "Benstøtte høyre",
        levArtNr = "29501",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "154654",
        navn = "Benstøtte høyre",
        levArtNr = "29502",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "154655",
        navn = "Benstøtte høyre",
        levArtNr = "29503",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "154656",
        navn = "Benstøtte høyre",
        levArtNr = "29504",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "154657",
        navn = "Benstøtte høyre",
        levArtNr = "29505",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "154658",
        navn = "Benstøtte høyre",
        levArtNr = "29506",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "154659",
        navn = "Benstøtte høyre",
        levArtNr = "29507",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "316217",
        navn = "Benstøtte høyre",
        levArtNr = "94859",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "316219",
        navn = "Benstøtte høyre",
        levArtNr = "29860",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "154660",
        navn = "Benstøtte venstre",
        levArtNr = "29508",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "154661",
        navn = "Benstøtte venstre",
        levArtNr = "29509",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "154662",
        navn = "Benstøtte venstre",
        levArtNr = "29510",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "154663",
        navn = "Benstøtte venstre",
        levArtNr = "29511",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "154664",
        navn = "Benstøtte venstre",
        levArtNr = "29512",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "154665",
        navn = "Benstøtte venstre",
        levArtNr = "29513",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "154666",
        navn = "Benstøtte venstre",
        levArtNr = "29514",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "154667",
        navn = "Benstøtte venstre",
        levArtNr = "29515",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "316218",
        navn = "Benstøtte venstre",
        levArtNr = "94861",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "316220",
        navn = "Benstøtte venstre",
        levArtNr = "94862",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "154670",
        navn = "Trekk benstøtte",
        levArtNr = "40886",
        kategori = "Annet",
        defaultAntall = 2,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "215101",
        navn = "Drivhjul 24''",
        levArtNr = "43183",
        kategori = "Hjul",
        defaultAntall = 2,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "316347",
        navn = "Drivhjul 24''",
        levArtNr = "93285",
        kategori = "Hjul",
        defaultAntall = 2,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "219466",
        navn = "Gaffel m/svinghjul 7''",
        levArtNr = "86134",
        kategori = "Svinghjul",
        defaultAntall = 2,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "268002",
        navn = "Gaffel m/svinghjul 7''",
        levArtNr = "90102",
        kategori = "Svinghjul",
        defaultAntall = 2,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "277498",
        navn = "Svinghjul 7''",
        levArtNr = "86135",
        kategori = "Svinghjul",
        defaultAntall = 2,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "138932",
        navn = "Bremse venstre",
        levArtNr = "26852",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "316333",
        navn = "Bremse venstre",
        levArtNr = "93674",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "138933",
        navn = "Bremse høyre",
        levArtNr = "26853",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "316334",
        navn = "Bremse høyre",
        levArtNr = "93675",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "154497",
        navn = "Setepute",
        levArtNr = "171806",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "154500",
        navn = "Setepute",
        levArtNr = "171809",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "154505",
        navn = "Setepute",
        levArtNr = "171813",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "154511",
        navn = "Setepute",
        levArtNr = "171818",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "154516",
        navn = "Setepute",
        levArtNr = "171822",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "154521",
        navn = "Setepute",
        levArtNr = "171826",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "154525",
        navn = "Setepute",
        levArtNr = "171829",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "154529",
        navn = "Setepute",
        levArtNr = "171830",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "181577",
        navn = "Trekk ryggpute",
        levArtNr = "367204",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "181579",
        navn = "Trekk ryggpute",
        levArtNr = "367205",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "181581",
        navn = "Trekk ryggpute",
        levArtNr = "367206",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "181583",
        navn = "Trekk ryggpute",
        levArtNr = "367207",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "181585",
        navn = "Trekk ryggpute",
        levArtNr = "367208",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "181587",
        navn = "Trekk ryggpute",
        levArtNr = "367209",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "181589",
        navn = "Trekk ryggpute",
        levArtNr = "367210",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "181592",
        navn = "Trekk ryggpute",
        levArtNr = "367227",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "316267",
        navn = "Trekk ryggpute",
        levArtNr = "367215",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "316269",
        navn = "Trekk ryggpute",
        levArtNr = "367216",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "181422",
        navn = "Hodestøtte",
        levArtNr = "81024",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "308897",
        navn = "Batteri",
        kategori = "Batteri",
        defaultAntall = 2,
        maksAntall = 2,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "286581",
        navn = "Lader",
        kategori = "Lader",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "307864",
        navn = "Hjul",
        kategori = "Hjul",
        defaultAntall = 4,
        maksAntall = 4,
        datoLagtTil = LocalDate.of(2025, 2, 19)
    ),
    Del(
        hmsnr = "279259",
        navn = "Setetrekk lang",
        levArtNr = "62989-60",
        kategori = "Annet",
        defaultAntall = 1,
        maksAntall = 1,
        datoLagtTil = LocalDate.of(2025, 2, 19)
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
        Hjelpemiddel(
            navn = hmsnrTilHjelpemiddel[it]?.navn
                ?: throw IllegalArgumentException("Mangler navn for hjelpemiddel $it"),
            hmsnr = it
        )
    }

    DelMedHjelpemidler(
        del = del,
        hjelpemidler = hjelpemidler
    )
}