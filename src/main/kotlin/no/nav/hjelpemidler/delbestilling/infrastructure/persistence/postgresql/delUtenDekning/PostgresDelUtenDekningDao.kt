package no.nav.hjelpemidler.delbestilling.infrastructure.persistence.postgresql.delUtenDekning

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.database.Row
import no.nav.hjelpemidler.delbestilling.common.Enhet
import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.config.isDev
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.Del
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.DelUtenDekningDao

private val log = KotlinLogging.logger {}

class PostgresDelUtenDekningDao(val tx: JdbcOperations) : DelUtenDekningDao {

    override fun lagreDelerUtenDekning(
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

    override fun hentUnikeEnheter(): List<Enhet> = tx.list(
        sql = """
            SELECT DISTINCT(enhetnr)
            FROM deler_uten_dekning
            WHERE rapportert_tidspunkt IS NULL
        """.trimIndent()
    ) { row -> Enhet.fraEnhetsnummer(row.string("enhetnr")) }

    override fun hentDelerTilRapportering(enhetnr: String): List<Del> {
        log.info { "Henter deler til rapportering for $enhetnr" }
        return tx.list(
            sql = """
                SELECT hmsnr, navn, SUM(antall_uten_dekning) as antall
                FROM deler_uten_dekning
                WHERE enhetnr = :enhetnr AND rapportert_tidspunkt IS NULL
                GROUP BY hmsnr, navn
            """.trimIndent(),
            queryParameters = mapOf("enhetnr" to enhetnr)
        ) { it.toDelUtenDekning() }
    }

    override fun markerDelerSomRapportert(enhet: Enhet) {
        log.info { "Marker deler som rapportert for enhet $enhet" }
        tx.update(
            """
                UPDATE deler_uten_dekning
                SET rapportert_tidspunkt = CURRENT_TIMESTAMP, sist_oppdatert = CURRENT_TIMESTAMP 
                WHERE enhetnr = :enhetnr AND rapportert_tidspunkt IS NULL
            """.trimIndent(),
            mapOf("enhetnr" to enhet.nummer)
        )
    }



    // Kun til testing i dev
    override fun markerDelerSomIkkeRapportert() {
        check(isDev()) { "markerDelerSomIkkeRapportert skal kun kalles i dev" }
        tx.update(
            sql = """
                UPDATE deler_uten_dekning
                SET rapportert_tidspunkt = NULL, sist_oppdatert = CURRENT_TIMESTAMP 
            """.trimIndent()
        )
    }
}

private fun Row.toDelUtenDekning() = Del(
    hmsnr = this.string("hmsnr"),
    navn = this.string("navn"),
    antall = this.int("antall"),
)