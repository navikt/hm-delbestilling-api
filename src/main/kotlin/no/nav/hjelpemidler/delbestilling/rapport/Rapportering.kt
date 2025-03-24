package no.nav.hjelpemidler.delbestilling.rapport

import ANTALL_UTLÅN
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import no.nav.hjelpemidler.delbestilling.Database
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingRepository
import no.nav.hjelpemidler.delbestilling.grunndata.GrunndataClient
import no.nav.hjelpemidler.delbestilling.slack.SlackClient

private val log = KotlinLogging.logger {}

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
                log.error(e) { "Rapportering feilet" }
            }
        }
    }

    suspend fun rapporterHjelpemidlerUtenDelbestillingOgMax10Utlån(
        slackClient: SlackClient,
        delbestillingRepository: DelbestillingRepository
    ) {
        val alleDelbestillinger = delbestillingRepository.hentDelbestillinger()
        val hjelpemiddelMedDelbestilling = alleDelbestillinger.map { it.delbestilling.hmsnr }.toSet()
        val hmsnrMedMax10UtlånOgUtenDelbestilling = ANTALL_UTLÅN
            .filter { it.value < 10 }
            .filter { it.key !in hjelpemiddelMedDelbestilling}
            .map { it.key to ANTALL_UTLÅN[it.key] }

        slackClient.rapporterHjelpemidlerUtenDelbestillingOgMax10Utlån(hmsnrMedMax10UtlånOgUtenDelbestilling)
    }
}