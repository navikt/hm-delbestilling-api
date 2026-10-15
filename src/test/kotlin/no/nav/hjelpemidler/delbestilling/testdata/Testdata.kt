package no.nav.hjelpemidler.delbestilling.testdata

import no.nav.hjelpemidler.delbestilling.common.Lager
import no.nav.hjelpemidler.domain.person.Fødselsdato
import no.nav.hjelpemidler.domain.person.Fødselsnummer
import no.nav.hjelpemidler.domain.person.år


object Testdata {

    val fnr = Fødselsnummer(40.år).value
    val bestillerFnr = Fødselsnummer(30.år).value

    val defaultHjmHmsnr = "097765" // Azalea
    val defaultHjmNavn = "Azalea" // Azalea
    val defaultHjmSerienr = "123456"
    val defaultDelHmsnr = "168802" // Armlene Azalea

    val defaultBrukernrstyrtHjmHmsnr = "299409"
    val defaultBrukernrstyrtHjmFnr = Fødselsnummer(50.år).value

    val delPåMinmax = "168802"
    val delPåLager = "168803"

    val kommunenummerOslo = "0301"
    val kommunenummerBergen = "4601"
    val defaultKommunenummer = kommunenummerOslo
    val defaultKommunenavn = "Oslo"

    val defaultEnhet = Lager.OSLO

    val isoERS = "122303"
    val isoPersonløfter = "123603"
}