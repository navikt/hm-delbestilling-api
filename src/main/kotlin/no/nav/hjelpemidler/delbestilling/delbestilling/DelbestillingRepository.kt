package no.nav.hjelpemidler.delbestilling.delbestilling

import kotliquery.Row
import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.database.pgObjectOf
import no.nav.hjelpemidler.delbestilling.common.Delbestilling
import no.nav.hjelpemidler.delbestilling.common.DelbestillingSak
import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.common.Serienr
import no.nav.hjelpemidler.delbestilling.common.Status
import no.nav.hjelpemidler.delbestilling.infrastructure.json
import no.nav.hjelpemidler.delbestilling.infrastructure.jsonMapper
import no.nav.hjelpemidler.delbestilling.infrastructure.roller.Organisasjon

class DelbestillingRepository(val tx: JdbcOperations) {

    fun lagreDelbestilling(
        bestillerFnr: String,
        brukerFnr: String,
        brukerKommunenr: String,
        delbestilling: Delbestilling,
        brukersKommunenavn: String,
        bestillersOrganisasjon: Organisasjon,
        bestillerType: BestillerType,
    ): Long = tx.updateAndReturnGeneratedKey(
        sql = """
            INSERT INTO delbestilling (brukers_kommunenr, fnr_bruker, fnr_bestiller, delbestilling_json, status, brukers_kommunenavn, bestillers_organisasjon, bestiller_type)
            VALUES (:brukers_kommunenr, :fnr_bruker, :fnr_bestiller, :delbestilling_json::jsonb, :status, :brukers_kommunenavn, :bestillers_organisasjon::jsonb, :bestiller_type)
        """.trimIndent(),
        queryParameters = mapOf(
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

    fun hentDelbestillinger(bestillerFnr: String): List<DelbestillingSak> = tx.list(
        sql = """
            SELECT * 
            FROM delbestilling
            WHERE fnr_bestiller = :fnr_bestiller
        """.trimIndent(),
        queryParameters = mapOf("fnr_bestiller" to bestillerFnr)
    ) { it.tilDelbestillingSak() }


    fun hentDelbestillingerForKommune(brukerKommunenr: String): List<DelbestillingSak> = tx.list(
        sql = """
            SELECT * 
            FROM delbestilling
            WHERE brukers_kommunenr = :brukers_kommunenr
        """.trimIndent(),
        queryParameters = mapOf("brukers_kommunenr" to brukerKommunenr)
    ) { it.tilDelbestillingSak() }

    fun hentDelbestillinger(hmsnr: Hmsnr, serienr: Serienr): List<DelbestillingSak> = tx.list(
        sql = """
            SELECT * 
            FROM delbestilling
            WHERE
                delbestilling_json ->> 'hmsnr' = :hmsnr AND
                delbestilling_json ->> 'serienr' = :serienr
        """.trimIndent(),
        queryParameters = mapOf("hmsnr" to hmsnr, "serienr" to serienr)
    ) { it.tilDelbestillingSak() }

    fun hentDelbestilling(saksnummer: Long): DelbestillingSak? = tx.singleOrNull(
        sql = """
            SELECT * 
            FROM delbestilling
            WHERE saksnummer = :saksnummer
        """.trimIndent(),
        queryParameters = mapOf("saksnummer" to saksnummer)
    ) { it.tilDelbestillingSak() }

    fun hentDelbestilling(oebsOrdrenummer: String): DelbestillingSak? = tx.singleOrNull(
        sql = """
            SELECT * 
            FROM delbestilling
            WHERE oebs_ordrenummer = :oebs_ordrenummer
        """.trimIndent(),
        mapOf("oebs_ordrenummer" to oebsOrdrenummer)
    ) { it.tilDelbestillingSak() }

    fun oppdaterDelbestillingSak(sak: DelbestillingSak) = tx.update(
        sql = """
            UPDATE delbestilling
            SET
                status = :status,
                oebs_ordrenummer = :oebs_ordrenummer,
                delbestilling_json = :delbestilling_json,
                sist_oppdatert = CURRENT_TIMESTAMP
            WHERE saksnummer = :saksnummer
        """.trimIndent(),
        queryParameters = mapOf(
            "status" to sak.status.name,
            "oebs_ordrenummer" to sak.oebsOrdrenummer,
            "delbestilling_json" to pgJsonbOf(sak.delbestilling),
            "saksnummer" to sak.saksnummer,
        )
    )
}

private fun Row.tilDelbestillingSak() = DelbestillingSak(
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