package no.nav.hjelpemidler.delbestilling.delbestilling

import no.nav.hjelpemidler.delbestilling.common.Delbestilling
import no.nav.hjelpemidler.delbestilling.common.DelbestillingSak
import java.util.UUID

data class XKLagerResponse(
    val xkLager: Boolean,
)

data class DelbestillingRequest(
    val delbestilling: Delbestilling,
)

data class DelbestillingResultat(
    val id: UUID,
    val feil: DelbestillingFeil? = null,
    val saksnummer: Long? = null,
    val delbestillingSak: DelbestillingSak? = null,
)

enum class DelbestillingFeil {
    INGET_UTLÅN,
    ULIK_GEOGRAFISK_TILKNYTNING,
    BRUKER_IKKE_FUNNET,
    BESTILLE_TIL_SEG_SELV,
    KAN_IKKE_BESTILLE,
    ULIK_ADRESSE_PDL_OEBS,
    FOR_MANGE_BESTILLINGER_SISTE_24_TIMER,
    LAGERENHET_IKKE_FUNNET,
}

enum class BestillerType {
    KOMMUNAL, IKKE_KOMMUNAL, BRUKERPASS
}
