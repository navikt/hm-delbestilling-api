package no.nav.hjelpemidler.delbestilling.delbestilling.anmodning

import kotliquery.Row
import no.nav.hjelpemidler.delbestilling.common.Enhet
import no.nav.hjelpemidler.delbestilling.common.Hmsnr

interface AnmodningRepository {
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
    fun lagreAnmodninger(rapport: Anmodningrapport)

    // Kun til testing i dev
    fun markerDelerSomIkkeRapportert()
}