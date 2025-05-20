package no.nav.hjelpemidler.delbestilling.common

/**
 * Vi mottar ikke lenger LUKKET fra OeBS. LUKKET ble sendt ved månedsslutt dersom alle dellinjene (ordrelinjene)
 * hadde status SKIPNINGSBEKREFTET. Med andre ord var LUKKET i praksis lik SKIPNINGSBEKREFTET.
 */
enum class Status {
    INNSENDT,
    REGISTRERT, // Fra OeBS
    KLARGJORT, // Fra OeBS
    DELVIS_SKIPNINGSBEKREFTET,
    SKIPNINGSBEKREFTET,
    ANNULLERT, // Fra OeBS
    LUKKET, // Fra OeBS
}

enum class DellinjeStatus {
    SKIPNINGSBEKREFTET,
}