package no.nav.hjelpemidler.delbestilling.rapport

import ANTALL_UTLÅN
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import no.nav.hjelpemidler.delbestilling.config.DatabaseConfig
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingRepository
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.GrunndataClient
import no.nav.hjelpemidler.delbestilling.infrastructure.slack.Slack

private val log = KotlinLogging.logger {}

class Rapportering {

    fun rapporter() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val grunndataClient = GrunndataClient(baseUrl = "https://finnhjelpemiddel.nav.no")
                val ds = DatabaseConfig.migratedDataSource
                val delbestillingRepository = DelbestillingRepository(ds)
                val slack = Slack(delbestillingRepository)
                rapporterHjelpemidlerUtenDelbestillingOgMax10Utlån(slack, delbestillingRepository)
            } catch (e: Exception) {
                log.error(e) { "Rapportering feilet" }
            }
        }
    }

    suspend fun rapporterHjelpemidlerUtenDelbestillingOgMax10Utlån(
        slack: Slack,
        delbestillingRepository: DelbestillingRepository
    ) {
        val telling = ANTALL_UTLÅN.map {
            it.key to Hjelpemiddel(it.key, it.value)
        }.toMap()

        val alleDelbestillinger = delbestillingRepository.hentDelbestillinger()

        alleDelbestillinger.forEach { bestilling ->
            if (bestilling.delbestilling.hmsnr in telling) {
                telling[bestilling.delbestilling.hmsnr]?.bestillinger++
            }
        }

        val resultat = telling.values.toList().sortedBy { it.utlån }

        slack.rapporterAntallBestillingerOgUtlånForHjelpemidler(resultat)
    }
}

data class Hjelpemiddel(
    val hmnsr: String,
    val utlån: Int,
    var bestillinger: Int = 0,
)