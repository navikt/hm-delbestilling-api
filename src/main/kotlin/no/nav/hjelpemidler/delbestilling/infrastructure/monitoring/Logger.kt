package no.nav.hjelpemidler.delbestilling.infrastructure.monitoring

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging

fun <T : Any> T.logger(): KLogger = KotlinLogging.logger(this::class.java.name)

fun logger2() =KotlinLogging.logger {}