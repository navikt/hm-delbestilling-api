package no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction

import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.postgresql.anmodning.PostgresAnmodningDao
import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.postgresql.delUtenDekning.PostgresDelUtenDekningDao
import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.postgresql.delbestilling.PostgresDelbestillingRepository


class TransactionScopeFactory {
    fun create(tx: JdbcOperations): TransactionScope {
        return TransactionScope(
            anmodningDao = PostgresAnmodningDao(tx),
            delUtenDekningDao = PostgresDelUtenDekningDao(tx),
            delbestillingRepository = PostgresDelbestillingRepository(tx),
        )
    }
}