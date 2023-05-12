package no.nav.hjelpemidler.delbestilling.delbestilling


val hjelpemiddelDeler = mapOf(
    "111111" to Hjelpemiddel(
        navn = "Panthera S3",
        hmsnr = "111111",
        deler = listOf(
            Del(
                navn = "Brems S3 høyre",
                beskrivelse = "Standard brems for S3 og U3",
                hmsnr = "252264",
                levArtNr = "4630002",
                img = "",
                kategori = "Brems"
            ),
            Del(
                navn = "Brems S3 venstre",
                beskrivelse = "Standard brems for S3 og U3",
                hmsnr = "252265",
                levArtNr = "4630001",
                img = "",
                kategori = "Brems"
            )
        )
    ),
    "222222" to Hjelpemiddel(
        navn = "Panthera U3",
        hmsnr = "111111",
        deler = listOf(
            Del(
                navn = "Brems S3 høyre",
                beskrivelse = "Standard brems for S3 og U3",
                hmsnr = "252264",
                levArtNr = "4630002",
                img = "",
                kategori = "Brems"
            ),
            Del(
                navn = "Schwalbe Marathon Plus 24 x 1",
                beskrivelse = "Punkteringsbeskyttet dekk til S3/U3 Light.",
                hmsnr = "252265",
                levArtNr = "4630001",
                img = "",
                kategori = "Dekk"
            )
        )
    )
)