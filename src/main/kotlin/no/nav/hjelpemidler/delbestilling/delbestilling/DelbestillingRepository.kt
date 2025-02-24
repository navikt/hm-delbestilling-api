package no.nav.hjelpemidler.delbestilling.delbestilling

import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.database.pgObjectOf
import no.nav.hjelpemidler.database.transactionAsync
import no.nav.hjelpemidler.delbestilling.json
import no.nav.hjelpemidler.delbestilling.jsonMapper
import no.nav.hjelpemidler.delbestilling.roller.Organisasjon
import javax.sql.DataSource

private val log = KotlinLogging.logger {}

class DelbestillingRepository(val ds: DataSource) {

    // OBS! Denne transaksjonen må sendes inn og brukes med tx.run() for å ha noen effekt.
    // using(sessionOf(ds)) { session -> ... } vil ikke bli en del av transaksjonen
    suspend inline fun <T> withTransaction(
        returnGeneratedKeys: Boolean = false,
        crossinline block: suspend (JdbcOperations) -> T,
    ): T = transactionAsync(ds, returnGeneratedKeys) { tx -> block(tx) }

    fun lagreDelbestilling(
        tx: JdbcOperations,
        bestillerFnr: String,
        brukerFnr: String,
        brukerKommunenr: String,
        delbestilling: Delbestilling, 
        brukersKommunenavn: String,
        bestillersOrganisasjon: Organisasjon,
        bestillerType: BestillerType,
    ): Long? {
        log.info { "Lagrer delbestilling '${delbestilling.id}'" }
        return tx.updateAndReturnGeneratedKey(
            """
                INSERT INTO delbestilling (brukers_kommunenr, fnr_bruker, fnr_bestiller, delbestilling_json, status, brukers_kommunenavn, bestillers_organisasjon, bestiller_type)
                VALUES (:brukers_kommunenr, :fnr_bruker, :fnr_bestiller, :delbestilling_json::jsonb, :status, :brukers_kommunenavn, :bestillers_organisasjon::jsonb, :bestiller_type)
            """.trimIndent(),
            mapOf(
                "brukers_kommunenr" to brukerKommunenr,
                "fnr_bruker" to brukerFnr,
                "fnr_bestiller" to bestillerFnr,
                "delbestilling_json" to jsonMapper.writeValueAsString(delbestilling),
                "status" to Status.INNSENDT.name,
                "brukers_kommunenavn" to brukersKommunenavn,
                "bestillers_organisasjon" to jsonMapper.writeValueAsString(bestillersOrganisasjon),
                "bestiller_type" to bestillerType,
            ),
        )
    }

    fun hentDelbestillinger(bestillerFnr: String): List<DelbestillingSak> = using(sessionOf(ds)) { session ->
        session.run(
            queryOf(
                """
                    SELECT * 
                    FROM delbestilling
                    WHERE fnr_bestiller = :fnr_bestiller
                """.trimIndent(),
                mapOf("fnr_bestiller" to bestillerFnr)
            ).map { it.toLagretDelbestilling() }.asList
        )
    }

    fun hentDelbestillingerForKommune(tx: JdbcOperations, brukers_kommunenr: String): List<DelbestillingSak> = using(sessionOf(ds)) { session ->
        session.run(
            queryOf(
                """
                    SELECT * 
                    FROM delbestilling
                    WHERE brukers_kommunenr = :brukers_kommunenr
                """.trimIndent(),
                mapOf("brukers_kommunenr" to brukers_kommunenr)
            ).map { it.toLagretDelbestilling() }.asList
        )
    }

    fun hentDelbestilling(tx: JdbcOperations, saksnummer: Long): DelbestillingSak? = tx.singleOrNull(
        """
            SELECT * 
            FROM delbestilling
            WHERE saksnummer = :saksnummer
        """.trimIndent(),
        mapOf("saksnummer" to saksnummer)
    ) { it.toLagretDelbestilling() }

    fun hentDelbestilling(tx: JdbcOperations, oebsOrdrenummer: String): DelbestillingSak? = tx.singleOrNull(
        """
                SELECT * 
                FROM delbestilling
                WHERE oebs_ordrenummer = :oebs_ordrenummer
            """.trimIndent(),
        mapOf("oebs_ordrenummer" to oebsOrdrenummer)
    ) { it.toLagretDelbestilling() }


    fun oppdaterStatus(tx: JdbcOperations, saksnummer: Long, status: Status) = try {
        tx.update(
            """
                UPDATE delbestilling
                SET status = :status, sist_oppdatert = CURRENT_TIMESTAMP
                WHERE saksnummer = :saksnummer
            """.trimIndent(),
            mapOf("status" to status.name, "saksnummer" to saksnummer)
        )
    } catch (e: Exception) {
        log.error(e) { "Oppdatering av status feilet" }
        throw e
    }

    fun oppdaterOebsOrdrenummer(tx: JdbcOperations, saksnummer: Long, oebsOrdrenummer: String) = try {
        tx.update(
            """
                UPDATE delbestilling
                SET oebs_ordrenummer = :oebs_ordrenummer, sist_oppdatert = CURRENT_TIMESTAMP
                WHERE saksnummer = :saksnummer
            """.trimIndent(),
            mapOf("oebs_ordrenummer" to oebsOrdrenummer, "saksnummer" to saksnummer)
        )
    } catch (e: Exception) {
        log.error(e) { "Oppdatering av oebs_ordrenummer feilet" }
        throw e
    }

    fun oppdaterDelbestilling(tx: JdbcOperations, saksnummer: Long, delbestilling: Delbestilling) = try {
        tx.update(
            """
                UPDATE delbestilling
                SET delbestilling_json = :delbestilling_json, sist_oppdatert = CURRENT_TIMESTAMP
                WHERE saksnummer = :saksnummer
            """.trimIndent(),
            mapOf("delbestilling_json" to pgJsonbOf(delbestilling), "saksnummer" to saksnummer)
        )
    } catch (e: Exception) {
        log.error(e) { "Oppdatering av delbestilling_json feilet" }
        throw e
    }
}

private fun Row.toLagretDelbestilling() = DelbestillingSak(
    this.long("saksnummer"),
    this.json("delbestilling_json"),
    this.localDateTime("opprettet"),
    Status.valueOf(this.string("status")),
    this.localDateTime("sist_oppdatert"),
    this.stringOrNull("oebs_ordrenummer"),
)

private fun <T> pgJsonbOf(value: T): Any =
    pgObjectOf(type = "jsonb", value = jsonMapper.writeValueAsString(value))