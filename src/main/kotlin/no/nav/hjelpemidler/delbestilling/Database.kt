package no.nav.hjelpemidler.delbestilling

import no.nav.hjelpemidler.database.createDataSource
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import javax.sql.DataSource

object Database {

    val migratedDataSource by lazy {
        val ds = createDataSource {
            hostname = Config.DB_HOST
            port = Config.DB_PORT.toInt()
            database = Config.DB_DATABASE
            username = Config.DB_USERNAME
            password = Config.DB_PASSWORD
            envVarPrefix = "DB"
        }

        migrate(ds)
        ds
    }

    fun migrate(dataSource: DataSource, initSql: String = ""): MigrateResult =
        Flyway.configure().dataSource(dataSource).initSql(initSql).load().migrate()
}