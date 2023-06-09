package no.nav.hjelpemidler.delbestilling

import com.zaxxer.hikari.HikariDataSource
import no.nav.hjelpemidler.delbestilling.Database.migrate
import org.flywaydb.core.Flyway

object TestDatabase {

    val testDataSource by lazy {
        val ds = HikariDataSource().apply {
            username = "sa"
            password = "sa"
            jdbcUrl = "jdbc:h2:~/test_db"
        }
        cleanAndMigrate(ds)
    }

    private fun clean(ds: HikariDataSource) =
        Flyway.configure().cleanDisabled(false).dataSource(ds).load().clean()

    fun cleanAndMigrate(ds: HikariDataSource): HikariDataSource {
        clean(ds)
        migrate(ds)
        return ds
    }
}