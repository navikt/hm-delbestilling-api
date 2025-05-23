package no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction

import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingRepository
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.AnmodningRepository

data class TransactionScope (
    val anmodningRepository: AnmodningRepository,
    val delbestillingRepository: DelbestillingRepository
)