package no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction

import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.AnmodningRepository
import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.postgresql.PostgresAnmodningRepository
import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.postgresql.PostgresDelbestillingRepository


class TransactionScopeFactory {
    fun create(tx: JdbcOperations): TransactionScope {
        return TransactionScope(
            anmodningRepository = PostgresAnmodningRepository(tx),
            delbestillingRepository = PostgresDelbestillingRepository(tx),
        )
    }
}