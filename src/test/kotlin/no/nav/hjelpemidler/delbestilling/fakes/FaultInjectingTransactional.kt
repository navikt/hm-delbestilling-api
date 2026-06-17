package no.nav.hjelpemidler.delbestilling.fakes

import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction.Transactional
import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction.TransactionScope

/**
 * Wrapper rundt Transactional som delegerer til den ekte implementasjonen,
 * men kan konfigureres til å kaste en feil på et bestemt kall (1-basert).
 * Nyttig for å teste feilhåndtering i kode som gjør flere DB-kall.
 */
class FaultInjectingTransactional(
    private val delegate: Transactional,
) : Transactional {
    private var callCount = 0
    var kastFeilPåKall: Int? = null
    var feil: Exception = RuntimeException("Injisert DB-feil")

    override suspend fun <T> invoke(returnGeneratedKeys: Boolean, block: suspend TransactionScope.() -> T): T {
        callCount++
        if (callCount == kastFeilPåKall) throw feil
        return delegate(returnGeneratedKeys, block)
    }
}
