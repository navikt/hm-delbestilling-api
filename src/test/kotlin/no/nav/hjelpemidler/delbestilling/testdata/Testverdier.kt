package no.nav.hjelpemidler.delbestilling.testdata

import no.nav.hjelpemidler.delbestilling.common.Lager
import no.nav.hjelpemidler.domain.person.Fødselsnummer
import no.nav.hjelpemidler.domain.person.år

val FNR_INNBYGGER = Fødselsnummer(40.år).value
val FNR_BESTILLER = Fødselsnummer(30.år).value

const val DEFAULT_HJM_HMSNR = "097765" // Azalea
const val DEFAULT_HJM_NAVN = "Azalea" // Azalea
const val DEFAULT_HJM_SERIENR = "123456"
const val DEFAULT_DEL_HMSNR = "168802" // Armlene Azalea

const val DEFAULT_BRUKERNRSTYRT_HJM_HMSNR = "299409"
val DEFAULT_BRUKERNRSTYRT_HJM_FNR = Fødselsnummer(50.år).value

const val DEL_PÅ_MINMAX = "168802"
const val DEL_PÅ_LAGER = "168803"

const val KOMMUNENUMMER_OSLO = "0301"
const val KOMMUNENUMMER_BERGEN = "4601"
const val DEFAULT_KOMMUNENUMMER = KOMMUNENUMMER_OSLO
const val DEFAULT_KOMMUNENAVN = "Oslo"

val DEFAULT_ENHET = Lager.OSLO

const val ISO_ERS = "122303"
const val ISO_PERSONLØFTER = "123603"
