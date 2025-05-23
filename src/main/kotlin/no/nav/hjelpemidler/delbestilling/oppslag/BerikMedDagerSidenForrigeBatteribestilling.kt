package no.nav.hjelpemidler.delbestilling.oppslag

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction.Transactional
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private val log = KotlinLogging.logger { }

class BerikMedDagerSidenForrigeBatteribestilling(
    private val transaction: Transactional,
) {

    suspend operator fun invoke(hjelpemiddel: Hjelpemiddel, serienr: String): Hjelpemiddel {
        if (hjelpemiddel.harBatteri()) {
            val antallDagerSidenForrigeBatteribestilling =
                beregnAntallDagerSidenSisteBatteribestilling(hjelpemiddel.hmsnr, serienr)
            log.info { "Siste batteribestilling ble gjort for $antallDagerSidenForrigeBatteribestilling dager siden." }
            return hjelpemiddel.medAntallDagerSidenSistBatteribestilling(antallDagerSidenForrigeBatteribestilling)
        }

        return hjelpemiddel
    }

    private suspend fun beregnAntallDagerSidenSisteBatteribestilling(hmsnr: String, serienr: String): Int? {
        return transaction {
            delbestillingRepository
                .hentDelbestillinger(hmsnr, serienr)
                .filter { it.delbestilling.harBatteri() }
                .maxByOrNull { it.opprettet }
                ?.opprettet
                ?.toLocalDate()
                ?.until(LocalDate.now(), ChronoUnit.DAYS)
                ?.toInt()
        }
    }
}