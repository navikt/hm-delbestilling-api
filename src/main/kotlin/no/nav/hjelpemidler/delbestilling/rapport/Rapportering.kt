package no.nav.hjelpemidler.delbestilling.rapport

import ANTALL_UTLÅN
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import no.nav.hjelpemidler.delbestilling.Database
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingRepository
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.GrunndataClient
import no.nav.hjelpemidler.delbestilling.infrastructure.monitoring.Logg
import no.nav.hjelpemidler.delbestilling.slack.SlackClient


class Rapportering {

    fun rapporter() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val grunndataClient = GrunndataClient(baseUrl = "https://finnhjelpemiddel.nav.no")
                val ds = Database.migratedDataSource
                val delbestillingRepository = DelbestillingRepository(ds)
                val slackClient = SlackClient(delbestillingRepository)
                rapporterHjelpemidlerUtenDelbestillingOgMax10Utlån(slackClient, delbestillingRepository)
            } catch (e: Exception) {
                Logg.error(e) { "Rapportering feilet" }
            }
        }
    }

    suspend fun rapporterHjelpemidlerUtenDelbestillingOgMax10Utlån(
        slackClient: SlackClient,
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

        slackClient.rapporterAntallBestillingerOgUtlånForHjelpemidler(resultat)
    }
}

data class Hjelpemiddel(
    val hmnsr: String,
    val utlån: Int,
    var bestillinger: Int = 0,
)