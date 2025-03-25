package no.nav.hjelpemidler.delbestilling.infrastructure.monitoring

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging

object Logg {
    private val logger: KLogger = KotlinLogging.logger {}

    fun debug(throwable: Throwable? = null, message: () -> Any?) = logger.debug(throwable, message)

    fun info(throwable: Throwable? = null, message: () -> Any?) = logger.info(throwable, message)

    fun warn(throwable: Throwable? = null, message: () -> Any?) = logger.warn(throwable, message)

    fun error(throwable: Throwable? = null, message: () -> Any?) = logger.error(throwable, message)

}