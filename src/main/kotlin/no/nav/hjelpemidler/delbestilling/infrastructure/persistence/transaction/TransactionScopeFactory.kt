package no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction

import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingRepository
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.AnmodningDao
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.DelUtenDekningDao
import java.time.Clock


class TransactionScopeFactory(private val clock: Clock) {
    fun create(tx: JdbcOperations): TransactionScope {
        return TransactionScope(
            anmodningDao = AnmodningDao(tx, clock),
            delUtenDekningDao = DelUtenDekningDao(tx),
            delbestillingRepository = DelbestillingRepository(tx),
        )
    }
}