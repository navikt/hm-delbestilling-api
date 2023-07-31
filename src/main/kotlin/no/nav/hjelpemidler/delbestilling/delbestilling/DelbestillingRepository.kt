package no.nav.hjelpemidler.delbestilling.delbestilling

import kotliquery.Session
import kotliquery.TransactionalSession
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import mu.KotlinLogging
import no.nav.hjelpemidler.database.transaction
import no.nav.hjelpemidler.delbestilling.json
import no.nav.hjelpemidler.delbestilling.jsonMapper
import javax.sql.DataSource

private val log = KotlinLogging.logger {}

class DelbestillingRepository(val ds: DataSource) {

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
    ): Long? {
        log.info { "Lagrer delbestilling '${delbestilling.id}'" }
        return tx.run(
            queryOf(
                """
                    INSERT INTO delbestilling (brukers_kommunenr, fnr_bruker, fnr_bestiller, delbestilling_json)
                    VALUES (:brukers_kommunenr, :fnr_bruker, :fnr_bestiller, :delbestilling_json::jsonb)
                """.trimIndent(),
                mapOf(
                    "brukers_kommunenr" to brukerKommunenr,
                    "fnr_bruker" to brukerFnr,
                    "fnr_bestiller" to bestillerFnr,
                    "delbestilling_json" to jsonMapper.writeValueAsString(delbestilling)
                ),
            ).asUpdateAndReturnGeneratedKey
        )
    }


    fun hentDelbestillinger(bestillerFnr: String): List<LagretDelbestilling> = using(sessionOf(ds)) { session ->
        session.run(
            queryOf(
                """
                    SELECT * 
                    FROM delbestilling
                    WHERE fnr_bestiller = :fnr_bestiller
                """.trimIndent(),
                mapOf("fnr_bestiller" to bestillerFnr)
            ).map {
                LagretDelbestilling(
                    it.long("saksnummer"),
                    it.json("delbestilling_json"),
                    it.localDateTime("opprettet")
                )
            }.asList
        )
    }
}
