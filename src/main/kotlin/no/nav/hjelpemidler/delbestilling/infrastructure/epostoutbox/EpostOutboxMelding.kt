package no.nav.hjelpemidler.delbestilling.infrastructure.epostoutbox

data class EpostOutboxMelding(
    val id: Long,
    val mottaker: String,
    val emne: String,
    val html: String,
    val attempts: Int,
    val alerted: Boolean,
)