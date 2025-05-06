package no.nav.hjelpemidler.delbestilling.infrastructure.oebs

interface OebsSink {
    fun sendDelbestilling(ordre: Ordre)
}