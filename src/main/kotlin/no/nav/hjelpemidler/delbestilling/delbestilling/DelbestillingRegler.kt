package no.nav.hjelpemidler.delbestilling.delbestilling

import no.nav.hjelpemidler.delbestilling.config.isDev

/**
 * Forretningsregler ved innsending av delbestilling som kan lempes på per miljø.
 */
data class DelbestillingRegler(
    val sjekkBestillingsrate: Boolean = true,
    val sjekkBestillingTilSegSelv: Boolean = true,
    val sjekkKommunenrPdlOebs: Boolean = true,
) {
    companion object {
        val PROD = DelbestillingRegler()

        // Lempeligere regler i dev for å kunne teste med testdata
        val DEV = DelbestillingRegler(
            sjekkBestillingsrate = false,
            sjekkBestillingTilSegSelv = false,
            sjekkKommunenrPdlOebs = false,
        )

        fun forMiljø(): DelbestillingRegler = if (isDev()) DEV else PROD
    }
}
