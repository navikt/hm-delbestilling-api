package no.nav.hjelpemidler.delbestilling.delbestilling

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import mu.KotlinLogging
import no.nav.hjelpemidler.delbestilling.jsonMapper
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
}