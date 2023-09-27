package no.nav.hjelpemidler.delbestilling.roller

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import mu.KotlinLogging
import no.nav.hjelpemidler.delbestilling.Database
import no.nav.hjelpemidler.delbestilling.delbestilling.Hjelpemiddel
import no.nav.hjelpemidler.delbestilling.delbestilling.toLagretDelbestilling
import no.nav.hjelpemidler.delbestilling.hjelpemidler.HjelpemiddelDeler
import javax.sql.DataSource

private val logg = KotlinLogging.logger {}

class RunOnStart(private val ds: DataSource = Database.migratedDataSource) {
    fun importNavn() {
        val linjer = object {}.javaClass.getResourceAsStream("/hjelpemidler.txt")!!.bufferedReader().readLines()

        val hjelpemidler = linjer.map { linje ->
            val (hmsnr, navn, type) = linje.split("|")
            Hjelpemiddel(hmsnr, navn, type)
        }

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
            val navnHovedprodukt = HjelpemiddelDeler.hentHjelpemiddelMedDeler(it.delbestilling.hmsnr)?.navn ?: "Ukjent"
            logg.info { "navnHovedprodukt: $navnHovedprodukt" }
        }
    }
}
