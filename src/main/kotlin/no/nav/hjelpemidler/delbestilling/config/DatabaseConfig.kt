package no.nav.hjelpemidler.delbestilling.config

import no.nav.hjelpemidler.configuration.EnvironmentVariable
import no.nav.hjelpemidler.configuration.External
import no.nav.hjelpemidler.database.PostgreSQL
import no.nav.hjelpemidler.database.createDataSource
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import javax.sql.DataSource

object DatabaseConfig {

    @External
    val DB_HOST by EnvironmentVariable

    @External
    val DB_PORT by EnvironmentVariable

    @External
    val DB_DATABASE by EnvironmentVariable

    @External
    val DB_USERNAME by EnvironmentVariable

    @External
    val DB_PASSWORD by EnvironmentVariable

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