package no.nav.hjelpemidler.delbestilling.fakes

import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction.TransactionScope
import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction.Transactional

class FakeTransaction(
    private val fakeScope: TransactionScope
) : Transactional {
    override suspend fun <T> invoke(returnGeneratedKeys: Boolean, block: suspend TransactionScope.() -> T): T {
        return fakeScope.block()
    }
}