package no.nav.hjelpemidler.delbestilling.infrastructure.persistence.postgresql.anmodning

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.database.JdbcOperations
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.AnmodningDao
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.Anmodningrapport

private val log = KotlinLogging.logger {}

class PostgresAnmodningDao(val tx: JdbcOperations) : AnmodningDao {
    override fun lagreAnmodninger(rapport: Anmodningrapport) {
        log.info { "Lagrer anmodninger for enhet ${rapport.enhet}" }

        rapport.anmodningsbehov.forEach { anmodning ->
            tx.update(
                """
                    INSERT INTO anmodninger (enhetnr, hmsnr, navn, antall_anmodet, antall_paa_lager, leverandornavn)
                    VALUES (:enhetnr, :hmsnr, :navn, :antall_anmodet, :antall_paa_lager, :leverandornavn)
                """.trimIndent(),
                mapOf(
                    "enhetnr" to rapport.enhet.nummer,
                    "hmsnr" to anmodning.hmsnr,
                    "navn" to anmodning.navn,
                    "antall_anmodet" to anmodning.antallSomMåAnmodes,
                    "antall_paa_lager" to anmodning.antallPåLager,
                    "leverandornavn" to anmodning.leverandørnavn,
                )
            )
        }
    }
}