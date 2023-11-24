package no.nav.hjelpemidler.delbestilling.delbestilling

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