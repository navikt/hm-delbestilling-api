package no.nav.hjelpemidler.delbestilling.hjelpemidler

import no.nav.hjelpemidler.delbestilling.delbestilling.Del
import no.nav.hjelpemidler.delbestilling.delbestilling.Hmsnr

val delerPerHjelpemiddel: Map<Hmsnr, List<Del>> = mapOf(
    "Comet" to listOf(
        "238403",
        "269827",
        "304570",
        "249612",
    ),
    "Orion" to listOf(
        "304570",
        "256635",
        "256634",
        "249612",
    ),
    "Minicrosser 125T" to listOf(
        "200842",
        "022005",
        "302543",
    ),
    "Minicrosser M" to listOf(
        "200842",
        "263773",
        "302543",
    ),
    "Minicrosser X" to listOf(
        "253277",
        "291358",
        "291356",
        "263773",
        "302543",
    ),
    "Minicrosser T" to listOf(
        "163943",
        "022005",
        "302543",
    ),
    "Molift personløfter" to listOf(
        "159563",
        "262846",
    ),
    "Panthera" to listOf(
        "150817",
        "278247",
        "223382",
        "178498",
        "184589",
        "234334",
        "196602",
        "202326",
        "211449",
        "232810",
        "278033",
    ),
    "Seng Opus" to listOf(
        "149965",
        "173483",
        "028152",
        "255826",
        "028158",
        "149963",
        "232849",
    ),
    "X850" to listOf(
        "157312",
        "157314",
        "196027",
        "309225",
    ),
    "X850S" to listOf(
        "309144",
        "309145",
        "309225",
    ),
).mapValues { it.value.map { hmsnr -> DELER[hmsnr]!! } }

