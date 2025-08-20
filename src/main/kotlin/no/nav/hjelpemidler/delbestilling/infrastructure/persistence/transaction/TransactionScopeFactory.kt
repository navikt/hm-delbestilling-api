package no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction

import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingRepository
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.AnmodningDao
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.DelUtenDekningDao


class TransactionScopeFactory {
    fun create(tx: JdbcOperations): TransactionScope {
        return TransactionScope(
            anmodningDao = AnmodningDao(tx),
            delUtenDekningDao = DelUtenDekningDao(tx),
            delbestillingRepository = DelbestillingRepository(tx),
        )
    }
}