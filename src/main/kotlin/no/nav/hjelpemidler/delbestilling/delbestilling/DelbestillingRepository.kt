package no.nav.hjelpemidler.delbestilling.delbestilling

import io.github.oshai.kotlinlogging.KotlinLogging
import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.database.pgObjectOf
import no.nav.hjelpemidler.database.transactionAsync
import no.nav.hjelpemidler.delbestilling.common.Delbestilling
import no.nav.hjelpemidler.delbestilling.common.DelbestillingSak
import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.common.Serienr
import no.nav.hjelpemidler.delbestilling.common.Status
import no.nav.hjelpemidler.delbestilling.infrastructure.json
import no.nav.hjelpemidler.delbestilling.infrastructure.jsonMapper
import no.nav.hjelpemidler.delbestilling.infrastructure.roller.Organisasjon
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
    ): Long {
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

    fun hentDelbestillingerForKommune(brukerKommunenr: String): List<DelbestillingSak> =
        using(sessionOf(ds)) { session ->
            session.run(
                queryOf(
                    """
                    SELECT * 
                    FROM delbestilling
                    WHERE brukers_kommunenr = :brukers_kommunenr
                """.trimIndent(),
                    mapOf("brukers_kommunenr" to brukerKommunenr)
                ).map { it.toLagretDelbestilling() }.asList
            )
        }

    fun hentDelbestillinger(hmsnr: Hmsnr, serienr: Serienr): List<DelbestillingSak> =
        using(sessionOf(ds)) { session ->
            session.run(
                queryOf(
                    """
                    SELECT * 
                    FROM delbestilling
                    WHERE
                        delbestilling_json ->> 'hmsnr' = :hmsnr AND
                        delbestilling_json ->> 'serienr' = :serienr
                    """.trimIndent(),
                    mapOf("hmsnr" to hmsnr, "serienr" to serienr)
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

    fun oppdaterDelbestillingSak(tx: JdbcOperations, sak: DelbestillingSak) = try {
        tx.update(
            """
                UPDATE delbestilling
                SET
                    status = :status,
                    oebs_ordrenummer = :oebs_ordrenummer,
                    delbestilling_json = :delbestilling_json,
                    sist_oppdatert = CURRENT_TIMESTAMP
                WHERE saksnummer = :saksnummer
            """.trimIndent(),
            mapOf(
                "status" to sak.status.name,
                "oebs_ordrenummer" to sak.oebsOrdrenummer,
                "delbestilling_json" to pgJsonbOf(sak.delbestilling),
                "saksnummer" to sak.saksnummer,
            )
        )
    } catch (e: Exception) {
        log.error(e) { "Feil ved oppdatering av delbestilling ${sak.saksnummer} med status ${sak.status} og oebsOrdrenummer ${sak.oebsOrdrenummer}" }
        throw e
    }
}

private fun Row.toLagretDelbestilling() = DelbestillingSak(
    saksnummer = this.long("saksnummer"),
    delbestilling = this.json("delbestilling_json"),
    opprettet = this.localDateTime("opprettet"),
    status = Status.valueOf(this.string("status")),
    sistOppdatert = this.localDateTime("sist_oppdatert"),
    oebsOrdrenummer = this.stringOrNull("oebs_ordrenummer"),
    brukersKommunenummer = this.string("brukers_kommunenr"),
    brukersKommunenavn = this.string("brukers_kommunenavn"),
)

private fun <T> pgJsonbOf(value: T): Any =
    pgObjectOf(type = "jsonb", value = jsonMapper.writeValueAsString(value))