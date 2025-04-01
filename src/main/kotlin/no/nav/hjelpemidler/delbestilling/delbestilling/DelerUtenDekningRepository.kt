package no.nav.hjelpemidler.delbestilling.delbestilling

import io.github.oshai.kotlinlogging.KotlinLogging
import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.database.transactionAsync
import javax.sql.DataSource

private val log = KotlinLogging.logger {}

class DelerUtenDekningRepository(val ds: DataSource) {

    // OBS! Denne transaksjonen må sendes inn og brukes med tx.run() for å ha noen effekt.
    // using(sessionOf(ds)) { session -> ... } vil ikke bli en del av transaksjonen
    suspend inline fun <T> withTransaction(
        returnGeneratedKeys: Boolean = false,
        crossinline block: suspend (JdbcOperations) -> T,
    ): T = transactionAsync(ds, returnGeneratedKeys) { tx -> block(tx) }

    fun lagreDelerUtenDekning(
        tx: JdbcOperations,
        saksnummer: Long,
        hmsnr: Hmsnr,
        navn: String,
        antallUtenDekning: Int,
        bukersKommunenummer: String,
        brukersKommunenavn: String,
        enhetnr: String,
    ): Long {
        log.info { "Lagrer del uten dekning $hmsnr ($antallUtenDekning)" }
        return tx.updateAndReturnGeneratedKey(
            """
                INSERT INTO deler_uten_dekning (saksnummer, hmsnr, navn, antall_uten_dekning, brukers_kommunenr, brukers_kommunenavn, enhetnr)
                VALUES (:saksnummer, :hmsnr, :navn, :antall_uten_dekning, :brukers_kommunenr, :brukers_kommunenavn, :enhetnr)
            """.trimIndent(),
            mapOf(
                "saksnummer" to saksnummer,
                "hmsnr" to hmsnr,
                "navn" to navn,
                "antall_uten_dekning" to antallUtenDekning,
                "brukers_kommunenr" to bukersKommunenummer,
                "brukers_kommunenavn" to brukersKommunenavn,
                "enhetnr" to enhetnr
            ),
        )
    }

    fun hentUnikeEnhetnrs(): List<String> =
        using(sessionOf(ds)) { session ->
            session.run(
                queryOf(
                    """
                    SELECT DISTINCT(enhetnr)
                    FROM deler_uten_dekning
                    WHERE rapportert_tidspunkt IS NULL
                """.trimIndent()
                ).map { row -> row.string("enhetnr") }.asList
            )
        }

    fun hentBestilteDeler(enhetnr: String): List<DelUtenDekning> =
        using(sessionOf(ds)) { session ->
            session.run(
                queryOf(
                    """
                    SELECT hmsnr, navn, SUM(antall_uten_dekning) as antall
                    FROM deler_uten_dekning
                    WHERE enhetnr = :enhetnr AND rapportert_tidspunkt IS NULL
                    GROUP BY hmsnr, navn
                """.trimIndent(),
                    mapOf("enhetnr" to enhetnr)
                ).map { it.toDelUtenDekning() }.asList
            )
        }

    fun markerDelerSomRapportert(enhetnr: String) {
        using(sessionOf(ds)) { session ->
            session.run(
                queryOf(
                    """
                    UPDATE deler_uten_dekning
                    SET rapportert_tidspunkt = CURRENT_TIMESTAMP, sist_oppdatert = CURRENT_TIMESTAMP 
                    WHERE enhetnr = :enhetnr AND rapportert_tidspunkt IS NULL
                    GROUP BY hmsnr, navn
                """.trimIndent(),
                    mapOf("enhetnr" to enhetnr)
                ).map { it.toDelUtenDekning() }.asList
            )
        }
    }

    private fun Row.toDelUtenDekning() = DelUtenDekning(
        hmsnr = this.string("hmsnr"),
        navn = this.string("navn"),
        antall = this.int("antall"),
    )
}
