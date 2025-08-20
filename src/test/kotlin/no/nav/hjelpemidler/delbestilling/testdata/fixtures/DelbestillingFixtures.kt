package no.nav.hjelpemidler.delbestilling.testdata.fixtures

import no.nav.hjelpemidler.delbestilling.common.Delbestilling
import no.nav.hjelpemidler.delbestilling.delbestilling.BestillerType
import no.nav.hjelpemidler.delbestilling.testdata.TestContext
import no.nav.hjelpemidler.delbestilling.testdata.Testdata
import no.nav.hjelpemidler.delbestilling.testdata.delbestilling
import no.nav.hjelpemidler.delbestilling.testdata.organisasjon
import java.time.LocalDateTime

suspend fun TestContext.gittDelbestilling(
    delbestilling: Delbestilling = delbestilling(),
    dagerSidenOpprettelse: Long? = null
) {
    transaction(returnGeneratedKeys = true) {
        val saksnummer = delbestillingRepository.lagreDelbestilling(
            bestillerFnr = Testdata.defaultFnr,
            brukerFnr = Testdata.defaultFnr,
            brukerKommunenr = Testdata.defaultKommunenummer,
            delbestilling = delbestilling,
            brukersKommunenavn = Testdata.defaultKommunenavn,
            bestillersOrganisasjon = organisasjon(),
            bestillerType = BestillerType.KOMMUNAL,
        )

        if (dagerSidenOpprettelse != null) {
            val opprettet = LocalDateTime.now().minusDays(dagerSidenOpprettelse)
            delbestillingRepository.tx.update(
                sql = """
            UPDATE delbestilling
            SET
                opprettet = :opprettet,
                sist_oppdatert = :opprettet
            WHERE saksnummer = :saksnummer
        """.trimIndent(),
                queryParameters = mapOf(
                    "opprettet" to opprettet,
                    "saksnummer" to saksnummer,
                )
            )
        }
    }
}
