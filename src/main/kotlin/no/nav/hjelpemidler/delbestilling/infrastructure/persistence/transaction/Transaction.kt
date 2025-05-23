package no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction

import no.nav.hjelpemidler.database.transactionAsync
import javax.sql.DataSource


class Transaction(
    private val dataSource: DataSource,
    private val scopeFactory: TransactionScopeFactory,
) : Transactional {

    override suspend operator fun <T> invoke(returnGeneratedKeys: Boolean, block: suspend TransactionScope.() -> T): T {
        return transactionAsync(
            dataSource = dataSource,
            returnGeneratedKeys = returnGeneratedKeys
        ) { tx ->
            val scope = scopeFactory.create(tx)
            scope.block()
        }
    }

}
