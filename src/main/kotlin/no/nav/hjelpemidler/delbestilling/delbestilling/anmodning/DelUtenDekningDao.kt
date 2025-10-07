package no.nav.hjelpemidler.delbestilling.delbestilling.anmodning

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.database.Row
import no.nav.hjelpemidler.delbestilling.common.Lager
import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.config.isDev

private val log = KotlinLogging.logger {}

class DelUtenDekningDao(val tx: JdbcOperations) {

    fun lagreDelerUtenDekning(
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
                INSERT INTO deler_uten_dekning (saksnummer, hmsnr, navn, antall_uten_dekning, brukers_kommunenr, brukers_kommunenavn, enhetnr, status)
                VALUES (:saksnummer, :hmsnr, :navn, :antall_uten_dekning, :brukers_kommunenr, :brukers_kommunenavn, :enhetnr, :status)
            """.trimIndent(),
            mapOf(
                "saksnummer" to saksnummer,
                "hmsnr" to hmsnr,
                "navn" to navn,
                "antall_uten_dekning" to antallUtenDekning,
                "brukers_kommunenr" to bukersKommunenummer,
                "brukers_kommunenavn" to brukersKommunenavn,
                "enhetnr" to enhetnr,
                "status" to DelerTilAnmodningStatus.AVVENTER.name
            ),
        )
    }

    fun hentUnikeEnheter(): List<Lager> = tx.list(
        sql = """
            SELECT DISTINCT(enhetnr)
            FROM deler_uten_dekning
            WHERE behandlet_tidspunkt IS NULL 
                AND status='AVVENTER'
        """.trimIndent()
    ) { row -> Lager.fraLagernummer(row.string("enhetnr")) }

    fun hentDelerTilRapportering(enhetnr: String): List<Del> {
        log.info { "Henter deler til rapportering for $enhetnr" }
        return tx.list(
            sql = """
                SELECT hmsnr, navn, SUM(antall_uten_dekning) as antall
                FROM deler_uten_dekning
                WHERE enhetnr = :enhetnr 
                    AND behandlet_tidspunkt IS NULL
                    AND status='AVVENTER'
                GROUP BY hmsnr, navn
            """.trimIndent(),
            queryParameters = mapOf("enhetnr" to enhetnr)
        ) { it.toDelUtenDekning() }
    }

    fun markerDelerSomBehandlet(lager: Lager, deler: List<Hmsnr>) {
        log.info { "Marker deler som rapportert for enhet $lager" }
        val indexedHmsnrs = deler.withIndex()
        tx.update(
            """
                UPDATE deler_uten_dekning
                SET behandlet_tidspunkt = CURRENT_TIMESTAMP,
                    status = 'BEHANDLET'
                WHERE enhetnr = :enhetnr 
                    AND behandlet_tidspunkt IS NULL 
                    AND hmsnr IN (${indexedHmsnrs.joinToString(",") { (index, _) -> ":hmsnr_$index" }})
            """.trimIndent(),
            mapOf(
                "enhetnr" to lager.nummer,
            ) + indexedHmsnrs.map { (index, hmsnr) -> "hmsnr_$index" to hmsnr }
        )
    }

    fun annulerSak(saksnummer: Long) {
        log.info { "Annulerer eventuelle deler_uten_dekning-rader som ikke er behandlet for sak $saksnummer" }
        tx.update(
            """
                UPDATE deler_uten_dekning
                SET status = 'ANNULERT'
                WHERE saksnummer = :saksnummer  
            """.trimIndent(),
            mapOf(
                "saksnummer" to saksnummer,
            )
        )
    }
}

private fun Row.toDelUtenDekning() = Del(
    hmsnr = this.string("hmsnr"),
    navn = this.string("navn"),
    antall = this.int("antall"),
)