package no.nav.hjelpemidler.delbestilling.grunndata

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hmsnrHjmTilHmsnrDeler
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hmsnrTilDel
import no.nav.hjelpemidler.delbestilling.hjelpemidler.data.hmsnrTilHjelpemiddel
import org.junit.jupiter.api.Test
import kotlin.test.Ignore

fun main() = runBlocking {
    val foo = hmsnrTilHjelpemiddel.size
    println(foo)

}

