package no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction

import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingRepository
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.AnmodningRepository


class TransactionScopeFactory {
    fun create(tx: JdbcOperations): TransactionScope {
        return TransactionScope(
            anmodningRepository = AnmodningRepository(tx),
            delbestillingRepository = DelbestillingRepository(tx),
        )
    }
}