package no.nav.hjelpemidler.delbestilling.rapportering

import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.ANMODNINGSBEHOV_SUBJECT
import no.nav.hjelpemidler.delbestilling.testdata.fixtures.gittDelbestillingUtenLagerdekning
import no.nav.hjelpemidler.delbestilling.testdata.runWithTestContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.stream.Stream
import kotlin.test.assertEquals


class RapporteringTest {

    companion object {
        @JvmStatic
        fun dagerProvider(): Stream<Arguments> = Stream.of(
            Arguments.of("2025-11-10T10:00:00Z", true),  // Mandag
            Arguments.of("2025-11-11T10:00:00Z", true),  // Tirsdag
            Arguments.of("2025-11-12T10:00:00Z", true),  // Onsdag
            Arguments.of("2025-11-13T10:00:00Z", true),  // Torsdag
            Arguments.of("2025-11-14T10:00:00Z", true),  // Fredag
            Arguments.of("2025-11-15T10:00:00Z", false), // Lørdag
            Arguments.of("2025-11-16T10:00:00Z", false)  // Søndag
        )
    }


    @ParameterizedTest
    @MethodSource("dagerProvider")
    fun `skal sende rapport om anmodningsbehov på ukedager i løpet av uken`(timestamp: String, skalKjøreJobb: Boolean) =
        runWithTestContext(fixedClock(timestamp)) {
            gittDelbestillingUtenLagerdekning()

            rapportering.kjørRapporteringsjobb()

            val jobbHarKjørt = emailClient.outbox.any { it.subject.contains(ANMODNINGSBEHOV_SUBJECT) }
            assertEquals(
                skalKjøreJobb,
                jobbHarKjørt,
                "Uventet kjøreresultat for tidspunkt: $timestamp"
            )
        }
}

private fun fixedClock(timestamp: String): Clock {
    val fixedInstant = Instant.parse(timestamp)
    return Clock.fixed(fixedInstant, ZoneOffset.systemDefault())
}
