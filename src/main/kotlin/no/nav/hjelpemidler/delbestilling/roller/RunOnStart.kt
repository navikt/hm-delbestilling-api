package no.nav.hjelpemidler.delbestilling.roller

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import mu.KotlinLogging
import no.nav.hjelpemidler.delbestilling.Database
import no.nav.hjelpemidler.delbestilling.delbestilling.toLagretDelbestilling
import javax.sql.DataSource

private val logg = KotlinLogging.logger {}

class RunOnStart(private val ds: DataSource = Database.migratedDataSource) {
    fun importNavn() {
        val delbestillinger = using(sessionOf(ds)) { session ->
            session.run(
                queryOf(
                    """
                    SELECT * 
                    FROM delbestilling
                    """.trimIndent(),
                ).map { it.toLagretDelbestilling() }.asList
            )
        }

        delbestillinger.forEach {
            logg.info { it }
        }
    }
}
