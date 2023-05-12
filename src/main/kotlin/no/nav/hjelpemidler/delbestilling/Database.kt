package no.nav.hjelpemidler.delbestilling

import com.zaxxer.hikari.HikariDataSource
import no.nav.hjelpemidler.database.createDataSource
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import javax.sql.DataSource

object Database {

    val migratedDataSource by lazy {
        val ds = if (isLocal()) {
            HikariDataSource().apply {
                username = "sa"
                password = "sa"
                jdbcUrl = "jdbc:h2:~/test_db"
            }
        } else {
            createDataSource {
                hostname = Config.DB_HOST
                port = Config.DB_PORT.toInt()
                database = Config.DB_DATABASE
                username = Config.DB_USERNAME
                password = Config.DB_PASSWORD
                envVarPrefix = "DB"
            }
        }

        migrate(ds)
        ds
    }

    private fun migrate(dataSource: DataSource, initSql: String = ""): MigrateResult =
        Flyway.configure().dataSource(dataSource).initSql(initSql).load().migrate()
}