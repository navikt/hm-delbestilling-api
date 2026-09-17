package no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction

import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingRepository
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.AnmodningDao
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.DelUtenDekningDao
import no.nav.hjelpemidler.delbestilling.infrastructure.epostoutbox.EpostOutboxDao
import no.nav.hjelpemidler.delbestilling.infrastructure.outbox.OutboxDao

data class TransactionScope(
    val anmodningDao: AnmodningDao,
    val delUtenDekningDao: DelUtenDekningDao,
    val delbestillingRepository: DelbestillingRepository,
    val epostOutboxDao: EpostOutboxDao,
    val outboxDao: OutboxDao,
)