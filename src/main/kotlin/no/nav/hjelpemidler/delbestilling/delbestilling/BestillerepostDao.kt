package no.nav.hjelpemidler.delbestilling.delbestilling

import no.nav.hjelpemidler.database.JdbcOperations

class BestillerepostDao(private val tx: JdbcOperations) {
    fun lagre(bestillerFnr: String, epost: String) = tx.update(
        sql = """
            INSERT INTO bestillerepost (fnr_bestiller, epost, oppdatert)
            VALUES (:fnr_bestiller, :epost, CURRENT_TIMESTAMP)
            ON CONFLICT (fnr_bestiller) DO UPDATE
            SET epost = :epost, oppdatert = CURRENT_TIMESTAMP
        """.trimIndent(),
        queryParameters = mapOf("fnr_bestiller" to bestillerFnr, "epost" to epost),
    )

    fun hent(bestillerFnr: String): String? = tx.singleOrNull(
        sql = """
            SELECT epost
            FROM bestillerepost
            WHERE fnr_bestiller = :fnr_bestiller
        """.trimIndent(),
        queryParameters = mapOf("fnr_bestiller" to bestillerFnr),
    ) { it.string("epost") }
}
