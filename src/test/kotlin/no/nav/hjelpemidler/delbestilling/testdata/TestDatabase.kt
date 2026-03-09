package no.nav.hjelpemidler.delbestilling.testdata

import no.nav.hjelpemidler.database.Testcontainers
import no.nav.hjelpemidler.database.createDataSource
import no.nav.hjelpemidler.database.createRole
import no.nav.hjelpemidler.database.flyway
import javax.sql.DataSource

object TestDatabase {
    val testDataSource by lazy {
        createDataSource(Testcontainers) {
            tag = "14-alpine"
        }
    }

    fun cleanAndMigratedDataSource(): DataSource {
        testDataSource.flyway {
            cleanDisabled(false)
            createRole("cloudsqliamuser")
        }.apply {
            clean()
            migrate()
        }
        return testDataSource
    }
}
