package no.nav.hjelpemidler.delbestilling.delbestilling.anmodning

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.database.Row
import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.common.Lager
import java.time.Clock
import java.time.YearMonth

private val log = KotlinLogging.logger {}

class AnmodningDao(private val tx: JdbcOperations, private val clock: Clock) {
    fun lagreAnmodninger(rapport: Anmodningrapport) {
        log.info { "Lagrer anmodninger for enhet ${rapport.lager}" }

        val nå = clock.instant()
        rapport.anmodningsbehov.forEach { anmodning ->
            tx.update(
                """
                    INSERT INTO anmodninger (enhetnr, hmsnr, navn, antall_anmodet, antall_paa_lager, leverandornavn, opprettet, sist_oppdatert)
                    VALUES (:enhetnr, :hmsnr, :navn, :antall_anmodet, :antall_paa_lager, :leverandornavn, :opprettet, :sist_oppdatert)
                """.trimIndent(),
                mapOf(
                    "enhetnr" to rapport.lager.nummer,
                    "hmsnr" to anmodning.hmsnr,
                    "navn" to anmodning.navn,
                    "antall_anmodet" to anmodning.antallSomMåAnmodes,
                    "antall_paa_lager" to anmodning.antallPåLager,
                    "leverandornavn" to anmodning.leverandørnavn,
                    "opprettet" to nå,
                    "sist_oppdatert" to nå,
                )
            )
        }
    }

    fun hentAnmodninger(lager: Lager, startMåned: YearMonth, sluttMåned: YearMonth): List<AnmodningEntity> {
        val startDato = startMåned.atDay(1).atStartOfDay()
        val sluttDato = sluttMåned.atEndOfMonth().plusDays(1).atStartOfDay()

        return tx.list(
            sql = """
            SELECT * 
            FROM anmodninger
            WHERE enhetnr = :enhetnr
            AND opprettet >= :startdato
            AND opprettet < :sluttdato
        """.trimIndent(),
            queryParameters = mapOf("enhetnr" to lager.nummer, "startdato" to startDato, "sluttdato" to sluttDato)
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