package no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction

import no.nav.hjelpemidler.database.transaction
import javax.sql.DataSource


class Transaction(
    private val dataSource: DataSource,
    private val scopeFactory: TransactionScopeFactory,
) : Transactional {

    override suspend operator fun <T> invoke(returnGeneratedKeys: Boolean, block: suspend TransactionScope.() -> T): T {
        return transaction(
            dataSource = dataSource,
            returnGeneratedKeys = returnGeneratedKeys
        ) { tx ->
            val scope = scopeFactory.create(tx)
            scope.block()
        }
    }

}
