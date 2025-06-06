package no.nav.hjelpemidler.delbestilling.delbestilling.anmodning

import no.nav.hjelpemidler.delbestilling.common.Enhet
import no.nav.hjelpemidler.delbestilling.common.Hmsnr

interface DelUtenDekningDao {
    fun lagreDelerUtenDekning(
        saksnummer: Long,
        hmsnr: Hmsnr,
        navn: String,
        antallUtenDekning: Int,
        bukersKommunenummer: String,
        brukersKommunenavn: String,
        enhetnr: String,
    ): Long

    fun hentUnikeEnheter(): List<Enhet>
    fun hentDelerTilRapportering(enhetnr: String): List<Del>
    fun markerDelerSomRapportert(enhet: Enhet)

    // Kun til testing i dev TODO: flytt denne funksjonaliteten til devtools, slik at vi slipper å blande det inn her.
    fun markerDelerSomIkkeRapportert()
}