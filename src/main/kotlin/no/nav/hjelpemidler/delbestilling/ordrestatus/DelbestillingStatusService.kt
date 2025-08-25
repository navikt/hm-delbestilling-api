package no.nav.hjelpemidler.delbestilling.ordrestatus

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.common.DelbestillingSak
import no.nav.hjelpemidler.delbestilling.common.DellinjeStatus
import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.common.Status
import no.nav.hjelpemidler.delbestilling.config.isDev
import no.nav.hjelpemidler.delbestilling.infrastructure.metrics.Metrics
import no.nav.hjelpemidler.delbestilling.infrastructure.oebs.Oebs
import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction.Transaction
import no.nav.hjelpemidler.delbestilling.infrastructure.slack.Slack
import java.time.LocalDate

private val log = KotlinLogging.logger {}

class DelbestillingStatusService(
    private val transaction: Transaction,
    private val oebs: Oebs,
    private val metrics: Metrics,
    private val slack: Slack,
) {

    suspend fun oppdaterStatus(saksnummer: Long, status: Status, oebsOrdrenummer: String) {
        val delbestilling = transaction {
            val lagretDelbestilling = hentDelbestillingEllerFeil(saksnummer) ?: return@transaction null

            val oppdatertDelbestilling = lagretDelbestilling
                .oppdaterOebsOrdrenummer(oebsOrdrenummer)
                .oppdaterStatus(status)

            delbestillingRepository.oppdaterDelbestillingSak(oppdatertDelbestilling)

            return@transaction oppdatertDelbestilling
        }

        logLagerstatusVedKlargjortVsInnsending(delbestilling, status)
    }

    suspend fun oppdaterDellinjeStatus(
        oebsOrdrenummer: String,
        status: DellinjeStatus,
        hmsnr: Hmsnr,
        datoOppdatert: LocalDate,
    ) {
        require(status == DellinjeStatus.SKIPNINGSBEKREFTET) { "Forventet status ${Status.SKIPNINGSBEKREFTET} for dellinje, men fikk status $status" }

        transaction {
            val lagretDelbestilling = delbestillingRepository.hentDelbestilling(oebsOrdrenummer)

            if (lagretDelbestilling == null) {
                log.debug { "Ignorerer oebsOrdrenummer $oebsOrdrenummer. Fant ikke tilhørende delbestilling, antar at det ikke tilhører en delbestilling." }
                return@transaction
            }

            if (lagretDelbestilling.status.ordinal >= Status.SKIPNINGSBEKREFTET.ordinal) {
                log.warn { "Forsøkte å sette dellinje på $oebsOrdrenummer til SKIPNINGSBEKREFTET, men ordren har status ${lagretDelbestilling.status}" }
                return@transaction
            }

            metrics.delSkipningsbekreftet(lagretDelbestilling, hmsnr, datoOppdatert)

            val oppdatertDelbestilling = lagretDelbestilling.oppdaterDellinjeStatus(status, hmsnr, datoOppdatert)

            delbestillingRepository.oppdaterDelbestillingSak(oppdatertDelbestilling)

            log.info { "Dellinje $hmsnr på sak ${lagretDelbestilling.saksnummer} (oebsnr $oebsOrdrenummer) oppdatert med status $status" }
        }
    }

    private suspend fun hentDelbestillingEllerFeil(saksnummer: Long): DelbestillingSak? {
        val delbestilling = transaction { delbestillingRepository.hentDelbestilling(saksnummer) }

        if (delbestilling != null) {
            return delbestilling
        }

        if (isDev()) {
            log.info { "Delbestilling $saksnummer finnes ikke i dev. Antar ugyldig testdata fra OeBS og skipper statusoppdatering." }
            return null
        } else {
            error("Kunne ikke oppdatere status for delbestilling $saksnummer fordi den ikke finnes.")
        }
    }


    // TODO Denne kan fjernes på sikt. Grei å beholde frem til "bestilling av del som ikke er på lager" er rullet ut til hele landet
    private suspend fun logLagerstatusVedKlargjortVsInnsending(delbestilling: DelbestillingSak?, status: Status) {
        try {
            if (delbestilling != null && status == Status.KLARGJORT) {
                val lagerstatus = oebs.hentLagerstatusForKommunenummerAsMap(
                    delbestilling.brukersKommunenummer,
                    delbestilling.delbestilling.deler.map { it.del.hmsnr })
                val lagerstatusVedInnsending =
                    delbestilling.delbestilling.deler.map { it.lagerstatusPåBestillingstidspunkt }
                log.info { "Lagerstatus for sak ${delbestilling.saksnummer} ved status=$status: $lagerstatus. Lagerstatus ved innsending: $lagerstatusVedInnsending" }

                val erReduksjonILagerstatus = lagerstatusVedInnsending.filterNotNull()
                    .any { vedInnsending ->
                        val nåværendeLagerstatus = (lagerstatus[vedInnsending.artikkelnummer]?.antallDelerPåLager ?: 99)
                        nåværendeLagerstatus < vedInnsending.antallDelerPåLager
                    }
                if (!erReduksjonILagerstatus) {
                    log.info { "Det var ikke reduksjon i lagerstatus mellom innsending av delbestilling og status=KLARGJORT for delbestilling ${delbestilling.saksnummer}. Sjekk om delbestilling-api har brukt feil lagerenhet." }
                    slack.varsleOmPotensieltFeilLager(delbestilling.saksnummer)
                }
            }
        } catch (t: Throwable) {
            log.info(t) { "Forsøk på logging av lagerstatus ved status $status feilet. Ignorerer." }
        }
    }

}