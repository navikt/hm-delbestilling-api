package no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction


interface Transactional {
    suspend operator fun <T> invoke(returnGeneratedKeys: Boolean = false, block: suspend TransactionScope.() -> T): T
}

