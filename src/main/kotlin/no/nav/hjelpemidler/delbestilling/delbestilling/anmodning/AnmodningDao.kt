package no.nav.hjelpemidler.delbestilling.delbestilling.anmodning

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.database.Row
import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.common.Lager
import java.time.YearMonth

private val log = KotlinLogging.logger {}

class AnmodningDao(val tx: JdbcOperations) {

    fun lagreAnmodninger(rapport: Anmodningrapport) {
        log.info { "Lagrer anmodninger for enhet ${rapport.lager}" }

        rapport.anmodningsbehov.forEach { anmodning ->
            tx.update(
                """
                    INSERT INTO anmodninger (enhetnr, hmsnr, navn, antall_anmodet, antall_paa_lager, leverandornavn)
                    VALUES (:enhetnr, :hmsnr, :navn, :antall_anmodet, :antall_paa_lager, :leverandornavn)
                """.trimIndent(),
                mapOf(
                    "enhetnr" to rapport.lager.nummer,
                    "hmsnr" to anmodning.hmsnr,
                    "navn" to anmodning.navn,
                    "antall_anmodet" to anmodning.antallSomMåAnmodes,
                    "antall_paa_lager" to anmodning.antallPåLager,
                    "leverandornavn" to anmodning.leverandørnavn,
                )
            )
        }
    }

    fun hentAnmodninger(lager: Lager, måned: YearMonth): List<AnmodningEntity> {
        val månedsstart = måned.atDay(1).atStartOfDay()
        val månedsslutt = måned.atEndOfMonth().plusDays(1).atStartOfDay()

        return tx.list(
            sql = """
            SELECT * 
            FROM anmodninger
            WHERE enhetnr = :enhetnr
            AND opprettet >= :startdato
            AND opprettet < :sluttdato
        """.trimIndent(),
            queryParameters = mapOf("enhetnr" to lager.nummer, "startdato" to månedsstart, "sluttdato" to månedsslutt)
        ) { it.tilAnmodningEntity() }
    }
}

data class AnmodningEntity(
    val enhetnr: String,
    val hmsnr: Hmsnr,
    val navn: String,
    val antallAnmodet: Int,
    val antallPaaLager: Int,
    val leverandornavn: String,
)

fun Row.tilAnmodningEntity(): AnmodningEntity = AnmodningEntity(
    enhetnr = string("enhetnr"),
    hmsnr = string("hmsnr"),
    navn = string("navn"),
    antallAnmodet = int("antall_anmodet"),
    antallPaaLager = int("antall_paa_lager"),
    leverandornavn = string("leverandornavn")
)