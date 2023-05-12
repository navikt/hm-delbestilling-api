package no.nav.hjelpemidler.delbestilling.delbestilling

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
        bestillerFnr: String,
        brukerFnr: String,
        brukerKommunenr: String,
        request: DelbestillingRequest
    ) = using(sessionOf(ds)) { session ->
        log.info { "Lagrer delbestilling '${request.id}'" }
        session.run(
            queryOf(
                """
                    INSERT INTO delbestilling (id, brukers_kommunenr, fnr_bruker, fnr_bestiller, delbestilling_json)
                    VALUES (:id, :brukers_kommunenr, :fnr_bruker, :fnr_bestiller, :delbestilling_json)
                """.trimIndent(),
                mapOf(
                    "id" to request.id,
                    "brukers_kommunenr" to brukerKommunenr,
                    "fnr_bruker" to brukerFnr,
                    "fnr_bestiller" to bestillerFnr,
                    "delbestilling_json" to jsonMapper.writeValueAsString(request)
                )
            ).asUpdate
        )
    }

    fun hentDelbestillinger(bestillerFnr: String) = using(sessionOf(ds)) { session ->
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
                Delbestilling(
                    id = UUID.fromString(it.string("id")),
                    brukerKommunenr = it.string("brukers_kommunenr"),
                    fnrBruker = it.string("fnr_bruker"),
                    fnrBestiller = it.string("fnr_bestiller"),
                    delbestillingJson = it.string("delbestilling_json"),
                    opprettet = it.string("opprettet_dato"),
                )
            }.asList
        )
    }
}