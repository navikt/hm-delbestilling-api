package no.nav.hjelpemidler.delbestilling

import no.nav.hjelpemidler.database.PostgreSQL
import no.nav.hjelpemidler.database.createDataSource
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import javax.sql.DataSource

object Database {

    val migratedDataSource by lazy {
        val ds = createDataSource(PostgreSQL) {
            envVarPrefix = "DB"
        }

        migrate(ds)
        ds
    }

    fun migrate(dataSource: DataSource, initSql: String = ""): MigrateResult =
        Flyway.configure().dataSource(dataSource).initSql(initSql).load().migrate()
}