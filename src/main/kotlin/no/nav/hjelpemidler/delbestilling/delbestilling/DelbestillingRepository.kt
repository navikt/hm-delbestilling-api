package no.nav.hjelpemidler.delbestilling.delbestilling

import kotliquery.Session
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import mu.KotlinLogging
import no.nav.hjelpemidler.delbestilling.jsonMapper
import java.util.UUID
import javax.sql.DataSource

private val log = KotlinLogging.logger {}

class DelbestillingRepository(private val ds: DataSource) {

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
                    VALUES (:brukers_kommunenr, :fnr_bruker, :fnr_bestiller, :delbestilling_json)
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
        log.info { "Henter delbestillinger for '$bestillerFnr'" }
        session.run(
            queryOf(
                """
                    SELECT * 
                    FROM delbestilling
                    WHERE fnr_bestiller = :fnr_bestiller
                """.trimIndent(),
                mapOf("fnr_bestiller" to bestillerFnr)
            ).map {
                val delbestilling = jsonMapper.readValue(it.string("delbestilling_json"), Delbestilling::class.java)
                val saksnummer = it.long("saksnummer")
                LagretDelbestilling(saksnummer, delbestilling)
            }.asList
        )
    }
}
