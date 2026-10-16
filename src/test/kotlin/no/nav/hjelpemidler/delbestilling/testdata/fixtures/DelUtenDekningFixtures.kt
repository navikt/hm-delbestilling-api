package no.nav.hjelpemidler.delbestilling.testdata.fixtures

import no.nav.hjelpemidler.database.Row
import no.nav.hjelpemidler.delbestilling.TestContext
import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.common.Lager
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.Del
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.DelUtenDekningDao
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.DelUtenDekningStatus
import no.nav.hjelpemidler.delbestilling.testdata.DEFAULT_DEL_HMSNR
import no.nav.hjelpemidler.delbestilling.testdata.DEFAULT_ENHET
import java.time.LocalDateTime

suspend fun TestContext.gittDelbestillingUtenLagerdekning(
) {
    oebslager.tømLager()
    opprettDelbestilling()
}

suspend fun TestContext.hentDelerUtenDekning(
    enhet: Lager = DEFAULT_ENHET,
): List<Del> {
    return transaction {
        delUtenDekningDao
            .hentDelerTilRapportering(enhet.nummer)
    }
}

suspend fun TestContext.hentDelUtenDekning(
    hmsnr: Hmsnr = DEFAULT_DEL_HMSNR,
): Del {
    return hentDelerUtenDekning().find { it.hmsnr == hmsnr }!!
}

suspend fun DelUtenDekningDao.hentRader(
    enhetnr: String = DEFAULT_ENHET.nummer
): List<DelUtenDekningEntity> {
    return tx.list(
        sql = """
                SELECT *
                FROM deler_uten_dekning
                WHERE enhetnr = :enhetnr
            """.trimIndent(),
        queryParameters = mapOf("enhetnr" to enhetnr)
    ) { it.toDelUtenDekningEntity() }
}

data class DelUtenDekningEntity(
    val saksnummer: Long,
    val hmsnr: Hmsnr,
    val status: DelUtenDekningStatus,
    val behandletTidspunkt: LocalDateTime,
)

private fun Row.toDelUtenDekningEntity() = DelUtenDekningEntity(
    saksnummer = this.long("saksnummer"),
    hmsnr = this.string("hmsnr"),
    status = DelUtenDekningStatus.valueOf(this.string("status")),
    behandletTidspunkt = this.localDateTime("behandlet_tidspunkt"),
)