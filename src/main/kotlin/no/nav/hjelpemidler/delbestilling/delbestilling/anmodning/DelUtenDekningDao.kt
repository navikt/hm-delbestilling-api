package no.nav.hjelpemidler.delbestilling.delbestilling.anmodning

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.database.Row
import no.nav.hjelpemidler.delbestilling.common.Lager
import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.config.isDev
import no.nav.hjelpemidler.delbestilling.config.isProd

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

    fun hentUnikeEnheter(): List<Lager> = tx.list(
        sql = """
            SELECT DISTINCT(enhetnr)
            FROM deler_uten_dekning
            WHERE rapportert_tidspunkt IS NULL
        """.trimIndent()
    ) { row -> Lager.fraLagernummer(row.string("enhetnr")) }

    fun hentDelerTilRapportering(enhetnr: String): List<Del> {
        // Hopper over ulike IDs i påvente av denne fiksen: https://trello.com/c/1cxdkRp3/472-bug-deler-som-har-f%C3%A5tt-lagerdekning-ila-dagen-og-dermed-ikke-skal-anmodes-sjekkes-p%C3%A5-nytt-hver-natt
        // Disse IDene er deler uten dekning som har fått lagerdekning mellom innsending og rapportering
        val skipListIds = when (isProd()) {
            true -> listOf(408)
            else -> emptyList()
        }

        log.info { "Henter deler til rapportering for $enhetnr, men med skipListIds: $skipListIds" }

        return tx.list(
            sql = """
                SELECT hmsnr, navn, SUM(antall_uten_dekning) as antall
                FROM deler_uten_dekning
                WHERE enhetnr = :enhetnr AND rapportert_tidspunkt IS NULL
                AND id NOT IN :skipListIds
                GROUP BY hmsnr, navn
            """.trimIndent(),
            queryParameters = mapOf("enhetnr" to enhetnr, "skipListIds" to skipListIds)
        ) { it.toDelUtenDekning() }
    }

    fun markerDelerSomRapportert(lager: Lager) {
        log.info { "Marker deler som rapportert for enhet $lager" }
        tx.update(
            """
                UPDATE deler_uten_dekning
                SET rapportert_tidspunkt = CURRENT_TIMESTAMP, sist_oppdatert = CURRENT_TIMESTAMP 
                WHERE enhetnr = :enhetnr AND rapportert_tidspunkt IS NULL
            """.trimIndent(),
            mapOf("enhetnr" to lager.nummer)
        )
    }



    // Kun til testing i dev
    fun markerDelerSomIkkeRapportert() {
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