package no.nav.hjelpemidler.delbestilling

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import javax.sql.DataSource

object Database {


    val migratedDataSource by lazy {

        val ds = Config.dataSource
//        val ds = HikariDataSource().apply {
//            username = user
//            password = pass
//            jdbcUrl = "jdbc:postgresql://$host:$port/$database"
//            maximumPoolSize = 10
//            minimumIdle = 1
//            idleTimeout = 10001
//            connectionTimeout = 1000
//            maxLifetime = 30001
//        }

        migrate(ds)

        ds
    }

    private fun migrate(dataSource: DataSource, initSql: String = ""): MigrateResult =
        Flyway.configure().dataSource(dataSource).initSql(initSql).load().migrate()
}