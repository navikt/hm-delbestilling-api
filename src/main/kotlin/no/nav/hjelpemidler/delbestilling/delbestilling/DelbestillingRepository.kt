package no.nav.hjelpemidler.delbestilling.delbestilling

import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.database.Row
import no.nav.hjelpemidler.database.pgObjectOf
import no.nav.hjelpemidler.delbestilling.common.Delbestilling
import no.nav.hjelpemidler.delbestilling.common.DelbestillingSak
import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.common.Lager
import no.nav.hjelpemidler.delbestilling.common.Serienr
import no.nav.hjelpemidler.delbestilling.common.Status
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
        lagerEnhet: Lager,
    ): Long = tx.updateAndReturnGeneratedKey(
        sql = """
            INSERT INTO delbestilling (brukers_kommunenr, fnr_bruker, fnr_bestiller, delbestilling_json, status, brukers_kommunenavn, bestillers_organisasjon, bestiller_type, enhetnr, enhetnavn)
            VALUES (:brukers_kommunenr, :fnr_bruker, :fnr_bestiller, :delbestilling_json::jsonb, :status, :brukers_kommunenavn, :bestillers_organisasjon::jsonb, :bestiller_type, :enhetnr, :enhetnavn)
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
            "enhetnr" to lagerEnhet.nummer,
            "enhetnavn" to lagerEnhet.navn,
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

    fun hentKommunenumreUtenEnhet(): List<String> = tx.list(
        sql = """
            SELECT DISTINCT(brukers_kommunenr)
            FROM delbestilling
            WHERE
                enhetnr IS NULL AND
                enhetnavn IS NULL
            ORDER BY brukers_kommunenr ASC
        """.trimIndent(),
    ) { it.string("brukers_kommunenr") }

    fun hentKlargjorteDelbestillinger(eldreEnnDager: Number): List<DelbestillingSak> = tx.list(
        sql = """
            SELECT *
            FROM delbestilling
            WHERE status = 'KLARGJORT'
              AND opprettet < NOW() - (:dager * INTERVAL '1 day')
            ORDER BY opprettet DESC;
        """.trimIndent(),
            queryParameters = mapOf("dager" to eldreEnnDager)
    ) { it.tilDelbestillingSak() }

    fun oppdaterDelbestillingSak(sak: DelbestillingSak) {
        tx.update(
            sql = """
            UPDATE delbestilling
            SET
                status = :status,
                oebs_ordrenummer = :oebs_ordrenummer,
                delbestilling_json = :delbestilling_json
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
    enhetnr = this.string("enhetnr"),
    enhetnavn = this.string("enhetnavn"),
)

private fun <T> pgJsonbOf(value: T): Any =
    pgObjectOf(type = "jsonb", value = jsonMapper.writeValueAsString(value))