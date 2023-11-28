package no.nav.hjelpemidler.delbestilling.delbestilling

import kotliquery.Row
import kotliquery.Session
import kotliquery.TransactionalSession
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import mu.KotlinLogging
import no.nav.hjelpemidler.database.pgObjectOf
import no.nav.hjelpemidler.database.transaction
import no.nav.hjelpemidler.delbestilling.json
import no.nav.hjelpemidler.delbestilling.jsonMapper
import javax.sql.DataSource

private val log = KotlinLogging.logger {}

class DelbestillingRepository(val ds: DataSource) {

    // OBS! Denne transaksjonen må sendes inn og brukes med tx.run() for å ha noen effekt.
    // using(sessionOf(ds)) { session -> ... } vil ikke bli en del av transaksjonen
    suspend inline fun <T> withTransaction(
        returnGeneratedKeys: Boolean = false,
        crossinline block: suspend (TransactionalSession) -> T,
    ): T = transaction(ds, returnGeneratedKeys) { tx -> block(tx) }

    fun lagreDelbestilling(
        tx: Session,
        bestillerFnr: String,
        brukerFnr: String,
        brukerKommunenr: String,
        delbestilling: Delbestilling,
        brukersKommunenavn: String,
    ): Long? {
        log.info { "Lagrer delbestilling '${delbestilling.id}'" }
        return tx.run(
            queryOf(
                """
                    INSERT INTO delbestilling (brukers_kommunenr, fnr_bruker, fnr_bestiller, delbestilling_json, status, brukers_kommunenavn)
                    VALUES (:brukers_kommunenr, :fnr_bruker, :fnr_bestiller, :delbestilling_json::jsonb, :status, :brukers_kommunenavn)
                """.trimIndent(),
                mapOf(
                    "brukers_kommunenr" to brukerKommunenr,
                    "fnr_bruker" to brukerFnr,
                    "fnr_bestiller" to bestillerFnr,
                    "delbestilling_json" to jsonMapper.writeValueAsString(delbestilling),
                    "status" to Status.INNSENDT.name,
                    "brukers_kommunenavn" to brukersKommunenavn,
                ),
            ).asUpdateAndReturnGeneratedKey
        )
    }

    fun hentDelbestillinger(tx: Session): List<DelbestillingSak> = tx.run(
        queryOf(
            """
                SELECT * 
                FROM delbestilling
            """.trimIndent()
        ).map { it.toLagretDelbestilling() }.asList
    )

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

    fun hentDelbestilling(tx: Session, saksnummer: Long): DelbestillingSak? = tx.run(
        queryOf(
            """
                SELECT * 
                FROM delbestilling
                WHERE saksnummer = :saksnummer
            """.trimIndent(),
            mapOf("saksnummer" to saksnummer)
        ).map { it.toLagretDelbestilling() }.asSingle
    )

    fun hentDelbestilling(tx: Session, oebsOrdrenummer: String): DelbestillingSak? = tx.run(
        queryOf(
            """
                SELECT * 
                FROM delbestilling
                WHERE oebs_ordrenummer = :oebs_ordrenummer
            """.trimIndent(),
            mapOf("oebs_ordrenummer" to oebsOrdrenummer)
        ).map { it.toLagretDelbestilling() }.asSingle
    )

    fun oppdaterStatus(tx: Session, saksnummer: Long, status: Status) = try {
        tx.run(
            queryOf(
                """
                UPDATE delbestilling
                SET status = :status, sist_oppdatert = CURRENT_TIMESTAMP
                WHERE saksnummer = :saksnummer
                """.trimIndent(),
                mapOf("status" to status.name, "saksnummer" to saksnummer)
            ).asUpdate
        )
    } catch (e: Exception) {
        log.error(e) { "Oppdatering av status feilet" }
        throw e
    }

    fun oppdaterOebsOrdrenummer(tx: Session, saksnummer: Long, oebsOrdrenummer: String) = try {
        tx.run(
            queryOf(
                """
                UPDATE delbestilling
                SET oebs_ordrenummer = :oebs_ordrenummer, sist_oppdatert = CURRENT_TIMESTAMP
                WHERE saksnummer = :saksnummer
                """.trimIndent(),
                mapOf("oebs_ordrenummer" to oebsOrdrenummer, "saksnummer" to saksnummer)
            ).asUpdate
        )
    } catch (e: Exception) {
        log.error(e) { "Oppdatering av oebs_ordrenummer feilet" }
        throw e
    }

    fun oppdaterDelbestilling(tx: Session, saksnummer: Long, delbestilling: Delbestilling) = try {
        tx.run(
            queryOf(
                """
                UPDATE delbestilling
                SET delbestilling_json = :delbestilling_json, sist_oppdatert = CURRENT_TIMESTAMP
                WHERE saksnummer = :saksnummer
                """.trimIndent(),
                mapOf("delbestilling_json" to pgJsonbOf(delbestilling), "saksnummer" to saksnummer)
            ).asUpdate
        )
    } catch (e: Exception) {
        log.error(e) { "Oppdatering av delbestilling_json feilet" }
        throw e
    }

    fun oppdaterDelbestillingUtenSistOppdatert(tx: Session, saksnummer: Long, delbestilling: Delbestilling) = try {
        tx.run(
            queryOf(
                """
                UPDATE delbestilling
                SET delbestilling_json = :delbestilling_json
                WHERE saksnummer = :saksnummer
                """.trimIndent(),
                mapOf("delbestilling_json" to pgJsonbOf(delbestilling), "saksnummer" to saksnummer)
            ).asUpdate
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